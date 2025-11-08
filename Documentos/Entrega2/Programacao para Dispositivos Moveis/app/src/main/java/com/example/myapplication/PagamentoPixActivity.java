package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Activity para pagamento PIX
 * Exibe QR Code e permite copiar código PIX
 */
public class PagamentoPixActivity extends AppCompatActivity {

    private ImageButton btnVoltar;
    private ImageView imgQRCode;
    private TextView tvCodigoPix, tvValorPagamento, tvStatusPagamento, tvInstrucoes;
    private MaterialButton btnCopiarCodigo, btnVerificarPagamento, btnCancelar;
    private MaterialCardView cardLoading, cardQRCode, cardStatus;
    private ProgressBar progressBar;

    private PixPagamentoManager pixManager;
    private SupabasePedidoManager pedidoManager;
    private PagamentoPix pagamentoAtual;
    private Pedido pedidoAtual;
    private String accessToken;

    private Handler handler;
    private Runnable verificadorPagamento;
    private static final int INTERVALO_VERIFICACAO = 5000; // 5 segundos
    private int tentativasVerificacao = 0;
    private static final int MAX_TENTATIVAS = 60; // 5 minutos (60 x 5s)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento_pix);

        inicializarViews();
        inicializarDados();
        configurarListeners();
        iniciarPagamento();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        imgQRCode = findViewById(R.id.imgQRCode);
        tvCodigoPix = findViewById(R.id.tvCodigoPix);
        tvValorPagamento = findViewById(R.id.tvValorPagamento);
        tvStatusPagamento = findViewById(R.id.tvStatusPagamento);
        tvInstrucoes = findViewById(R.id.tvInstrucoes);
        btnCopiarCodigo = findViewById(R.id.btnCopiarCodigo);
        btnVerificarPagamento = findViewById(R.id.btnVerificarPagamento);
        btnCancelar = findViewById(R.id.btnCancelar);
        cardLoading = findViewById(R.id.cardLoading);
        cardQRCode = findViewById(R.id.cardQRCode);
        cardStatus = findViewById(R.id.cardStatus);
        progressBar = findViewById(R.id.progressBar);
    }

    private void inicializarDados() {
        pixManager = PixPagamentoManager.getInstance(this);
        pedidoManager = SupabasePedidoManager.getInstance(this);
        handler = new Handler(Looper.getMainLooper());

        // Receber dados do pedido via Intent
        String pedidoId = getIntent().getStringExtra("pedido_id");
        double valor = getIntent().getDoubleExtra("valor", 0.0);
        accessToken = getIntent().getStringExtra("access_token");

        // Verificar se os dados foram passados corretamente
        if (pedidoId == null || valor <= 0 || accessToken == null) {
            Toast.makeText(this, "Erro: Dados do pedido inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Criar pedido temporário com os dados recebidos
        SessionManager sessionManager = SessionManager.getInstance(this);

        pedidoAtual = new Pedido();
        pedidoAtual.setStudentId(sessionManager.getUserId());
        pedidoAtual.setStudentName(sessionManager.getUserEmail());
        pedidoAtual.setTotal(valor); // ⭐ ADICIONE o método setTotal() na classe Pedido

        // Armazenar o ID do pedido (opcional - para referência)
        // pedidoAtual.setId(pedidoId); // Se precisar
    }
    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> mostrarDialogoCancelar());

        btnCopiarCodigo.setOnClickListener(v -> {
            if (pagamentoAtual != null && pagamentoAtual.getQrCodeTexto() != null) {
                copiarCodigoPix();
            }
        });

        btnVerificarPagamento.setOnClickListener(v -> verificarPagamentoManual());

        btnCancelar.setOnClickListener(v -> mostrarDialogoCancelar());
    }

    private void iniciarPagamento() {
        mostrarLoading(true);

        // Use o email real se disponível
        SessionManager sessionManager;
        sessionManager = SessionManager.getInstance(this);
        String emailReal = sessionManager.getUserEmail(); // Implemente este método

        pedidoAtual.setStudentName(emailReal);

        pixManager.criarPagamentoPix(pedidoAtual, new PixPagamentoManager.PagamentoCallback() {
            @Override
            public void onSuccess(PagamentoPix pagamento) {
                runOnUiThread(() -> {
                    pagamentoAtual = pagamento;
                    mostrarLoading(false);
                    exibirQRCode(pagamento);
                    iniciarVerificacaoAutomatica();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    mostrarLoading(false);
                    mostrarErro(erro);
                });
            }
        });
    }

    private void exibirQRCode(PagamentoPix pagamento) {
        cardQRCode.setVisibility(View.VISIBLE);

        // Valor
        tvValorPagamento.setText(PedidoUtils.formatarPreco(pagamento.getValor()));

        // Código PIX
        tvCodigoPix.setText(pagamento.getQrCodeTexto());

        // Gerar e exibir QR Code
        if (pagamento.getQrCode() != null) {
            // Se vier Base64 do servidor
            try {
                byte[] decodedString = Base64.decode(pagamento.getQrCode(), Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgQRCode.setImageBitmap(bitmap);
            } catch (Exception e) {
                gerarQRCodeLocal(pagamento.getQrCodeTexto());
            }
        } else if (pagamento.getQrCodeTexto() != null) {
            // Gerar localmente
            gerarQRCodeLocal(pagamento.getQrCodeTexto());
        }

        // Status
        atualizarStatus("Aguardando Pagamento", "#FF9800");

        // Instruções
        tvInstrucoes.setText("1. Abra o app do seu banco\n" +
                "2. Escolha pagar com PIX\n" +
                "3. Escaneie o QR Code ou cole o código\n" +
                "4. Confirme o pagamento");
    }

    private void gerarQRCodeLocal(String texto) {
        Bitmap qrCode = pixManager.gerarQRCode(texto, 512);
        if (qrCode != null) {
            imgQRCode.setImageBitmap(qrCode);
        }
    }

    private void copiarCodigoPix() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Código PIX", pagamentoAtual.getQrCodeTexto());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "✅ Código PIX copiado!", Toast.LENGTH_SHORT).show();
        btnCopiarCodigo.setText("✓ COPIADO");

        handler.postDelayed(() -> {
            btnCopiarCodigo.setText("📋 COPIAR CÓDIGO PIX");
        }, 2000);
    }

    private void iniciarVerificacaoAutomatica() {
        verificadorPagamento = new Runnable() {
            @Override
            public void run() {
                if (pagamentoAtual != null && pagamentoAtual.isPendente()) {
                    verificarStatusPagamento();

                    tentativasVerificacao++;
                    if (tentativasVerificacao < MAX_TENTATIVAS) {
                        handler.postDelayed(this, INTERVALO_VERIFICACAO);
                    } else {
                        mostrarDialogoTimeout();
                    }
                }
            }
        };

        handler.postDelayed(verificadorPagamento, INTERVALO_VERIFICACAO);
    }

    private void verificarStatusPagamento() {
        if (pagamentoAtual == null) return;

        pixManager.verificarStatusPagamento(pagamentoAtual.getTransactionId(),
                new PixPagamentoManager.PagamentoCallback() {
                    @Override
                    public void onSuccess(PagamentoPix pagamento) {
                        runOnUiThread(() -> {
                            pagamentoAtual.setStatus(pagamento.getStatus());

                            if (pagamento.isPago()) {
                                pararVerificacao();
                                pagamentoAprovado();
                            } else if (pagamento.isCancelado()) {
                                pararVerificacao();
                                pagamentoCancelado();
                            }
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        // Continuar verificando em caso de erro temporário
                    }
                });
    }

    private void verificarPagamentoManual() {
        btnVerificarPagamento.setEnabled(false);
        btnVerificarPagamento.setText("Verificando...");

        verificarStatusPagamento();

        handler.postDelayed(() -> {
            btnVerificarPagamento.setEnabled(true);
            btnVerificarPagamento.setText("🔄 VERIFICAR PAGAMENTO");
        }, 3000);
    }

    private void pagamentoAprovado() {
        atualizarStatus("✅ Pagamento Aprovado!", "#4CAF50");

        // Atualizar pedido no Supabase
        if (pedidoAtual != null && accessToken != null) {
            // Aqui você atualizaria o status do pedido para "PAGO"
            // pedidoManager.atualizarStatusPedido(pedidoAtual.getId(), "CONFIRMADO", accessToken);
        }

        new AlertDialog.Builder(this)
                .setTitle("🎉 Pagamento Confirmado!")
                .setMessage("Seu pagamento foi aprovado com sucesso!\n\nPedido: #" +
                        (pedidoAtual != null ? pedidoAtual.getCode() : "N/A"))
                .setPositiveButton("Ver Pedido", (dialog, which) -> {
                    Intent intent = new Intent(this, MeusPedidosActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void pagamentoCancelado() {
        atualizarStatus("❌ Pagamento Cancelado", "#F44336");

        new AlertDialog.Builder(this)
                .setTitle("Pagamento Cancelado")
                .setMessage("O pagamento não foi concluído.")
                .setPositiveButton("Tentar Novamente", (dialog, which) -> {
                    recreate(); // Reiniciar activity
                })
                .setNegativeButton("Voltar", (dialog, which) -> finish())
                .show();
    }

    private void mostrarDialogoCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pagamento")
                .setMessage("Deseja cancelar o pagamento?\n\nO pedido será cancelado.")
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> {
                    pararVerificacao();
                    finish();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void mostrarDialogoTimeout() {
        new AlertDialog.Builder(this)
                .setTitle("Tempo Esgotado")
                .setMessage("O tempo para pagamento expirou.\n\nDeseja gerar novo QR Code?")
                .setPositiveButton("Sim", (dialog, which) -> recreate())
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .show();
    }

    private void atualizarStatus(String texto, String cor) {
        tvStatusPagamento.setText(texto);
        tvStatusPagamento.setTextColor(android.graphics.Color.parseColor(cor));
    }

    private void mostrarLoading(boolean mostrar) {
        cardLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        cardQRCode.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    private void mostrarErro(String erro) {
        new AlertDialog.Builder(this)
                .setTitle("Erro no Pagamento")
                .setMessage(erro)
                .setPositiveButton("Tentar Novamente", (dialog, which) -> iniciarPagamento())
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .show();
    }

    private void pararVerificacao() {
        if (handler != null && verificadorPagamento != null) {
            handler.removeCallbacks(verificadorPagamento);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararVerificacao();
    }


}