package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.Locale;

public class AdminEditarProdutoActivity extends AppCompatActivity {
    private static final String TAG = "AdminEditarProduto";

    private EditText editNome, editValor, editDetalhes, editEstoque;
    private Spinner spinnerCategoria;
    private ImageView imageViewPreview;
    private Button btnSalvar, btnExcluir, btnSelecionarImagem, btnVoltar, btnRemoverImagem;

    private SupabaseClient supabaseClient;
    private Produto produtoOriginal;
    private Uri novaImagemUri;
    private boolean removerImagem = false;

    private ActivityResultLauncher<String> selecionarImagemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_produto);

        // Inicializar componentes
        editNome = findViewById(R.id.EditTextNomeProduto);
        editValor = findViewById(R.id.EditTextValorProduto);
        editDetalhes = findViewById(R.id.EditTextDetalhesProduto);
        editEstoque = findViewById(R.id.EditTextEstoque);
        spinnerCategoria = findViewById(R.id.SpinnerCategoria);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        btnSalvar = findViewById(R.id.ButtonSalvarAlteracoes);
        btnExcluir = findViewById(R.id.ButtonExcluir);
        btnSelecionarImagem = findViewById(R.id.buttonSelecionarImagem);
        btnVoltar = findViewById(R.id.botaoVoltar);

        // REMOVER ou OCULTAR o EditText da imagem
        EditText editImagem = findViewById(R.id.EditTextImagemProduto);
        if (editImagem != null) {
            editImagem.setVisibility(View.GONE); // Ocultar campo de texto
        }

        supabaseClient = SupabaseClient.getInstance(this);

        // Configurar spinner de categorias
        String[] categorias = {"Bebidas", "Pratos Principais", "Sobremesas", "Entradas", "Lanches"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        // Receber produto da intent
        Intent intent = getIntent();
        produtoOriginal = (Produto) intent.getSerializableExtra("produtoInfo");

        if (produtoOriginal == null) {
            Toast.makeText(this, "Erro ao carregar produto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Produto carregado - ID: " + produtoOriginal.getId() +
                ", Nome: " + produtoOriginal.getNome());

        // Preencher campos com dados do produto
        preencherCampos();

        // Configurar launcher para seleção de imagem
        selecionarImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        novaImagemUri = uri;
                        removerImagem = false;
                        imageViewPreview.setImageURI(uri);
                        imageViewPreview.setVisibility(View.VISIBLE);
                        btnSelecionarImagem.setText("Alterar Imagem");
                        Log.d(TAG, "Nova imagem selecionada");
                    }
                }
        );

        // Botões
        btnVoltar.setOnClickListener(v -> finish());

        btnSelecionarImagem.setOnClickListener(v -> {
            selecionarImagemLauncher.launch("image/*");
        });

        btnSalvar.setOnClickListener(v -> salvarAlteracoes());

        btnExcluir.setOnClickListener(v -> confirmarExclusao());
    }

    private void preencherCampos() {
        editNome.setText(produtoOriginal.getNome());
        editValor.setText(String.format(Locale.getDefault(), "%.2f", produtoOriginal.getPreco()));
        editDetalhes.setText(produtoOriginal.getDescricao());
        editEstoque.setText(String.valueOf(produtoOriginal.getEstoque()));

        // Categoria (ID - 1 porque spinner começa em 0)
        int categoriaIndex = produtoOriginal.getCategoria() - 1;
        if (categoriaIndex >= 0 && categoriaIndex < spinnerCategoria.getCount()) {
            spinnerCategoria.setSelection(categoriaIndex);
        }

        // Carregar imagem atual
        String caminhoImagem = produtoOriginal.getCaminhoImagem();
        if (caminhoImagem != null && !caminhoImagem.isEmpty() && !caminhoImagem.equals("EMPTY")) {
            Glide.with(this)
                    .load(caminhoImagem)
                    .placeholder(R.drawable.sem_imagem)
                    .error(R.drawable.sem_imagem)
                    .into(imageViewPreview);
            imageViewPreview.setVisibility(View.VISIBLE);
            btnSelecionarImagem.setText("Alterar Imagem");
        } else {
            imageViewPreview.setImageResource(R.drawable.sem_imagem);
            imageViewPreview.setVisibility(View.VISIBLE);
            btnSelecionarImagem.setText("Selecionar Imagem");
        }
    }

    private void salvarAlteracoes() {
        // Validar campos
        String nome = editNome.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim().replace(",", ".");
        String detalhes = editDetalhes.getText().toString().trim();
        String estoqueStr = editEstoque.getText().toString().trim();

        if (nome.isEmpty() || valorStr.isEmpty() || estoqueStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            int estoque = Integer.parseInt(estoqueStr);
            int categoria = spinnerCategoria.getSelectedItemPosition() + 1;

            if (valor <= 0) {
                Toast.makeText(this, "O valor deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (estoque < 0) {
                Toast.makeText(this, "O estoque não pode ser negativo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Criar produto atualizado mantendo a imagem atual
            Produto produtoAtualizado = new Produto(
                    produtoOriginal.getId(),
                    nome,
                    valor,
                    detalhes,
                    produtoOriginal.getCaminhoImagem(), // Mantém a imagem atual inicialmente
                    estoque,
                    categoria
            );

            Log.d(TAG, "Preparando atualização - ID: " + produtoAtualizado.getId());

            // Se selecionou nova imagem, fazer upload primeiro
            if (novaImagemUri != null) {
                Toast.makeText(this, "Fazendo upload da imagem...", Toast.LENGTH_SHORT).show();

                // CORREÇÃO: Nome correto do bucket é "IMAGEM"
                supabaseClient.uploadImageToBucket(novaImagemUri, "IMAGEM", new SupabaseClient.SupabaseCallback<String>() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        Log.d(TAG, "✓ Upload concluído. URL: " + imageUrl);
                        produtoAtualizado.setCaminhoImagem(imageUrl);
                        atualizarProdutoNoBanco(produtoAtualizado);
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "❌ Erro no upload: " + error);
                            Toast.makeText(AdminEditarProdutoActivity.this,
                                    "Erro ao fazer upload da imagem: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                // Sem nova imagem, atualizar direto
                atualizarProdutoNoBanco(produtoAtualizado);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro ao converter valores", e);
            Toast.makeText(this, "Valores inválidos. Verifique preço e estoque.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarProdutoNoBanco(Produto produto) {
        Log.d(TAG, "=== ATUALIZANDO PRODUTO ===");
        Log.d(TAG, "ID: " + produto.getId());
        Log.d(TAG, "Nome: " + produto.getNome());
        Log.d(TAG, "Preço: " + produto.getPreco());
        Log.d(TAG, "Estoque: " + produto.getEstoque());
        Log.d(TAG, "Categoria: " + produto.getCategoria());
        Log.d(TAG, "Imagem: " + (produto.getCaminhoImagem() != null ? "SIM" : "NÃO"));

        supabaseClient.updateProduct(produto, new SupabaseClient.SupabaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto produtoAtualizado) {
                runOnUiThread(() -> {
                    Log.d(TAG, "✓ Produto atualizado com sucesso!");
                    Toast.makeText(AdminEditarProdutoActivity.this,
                            "Produto atualizado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Erro ao atualizar: " + error);

                    String mensagem = "Erro ao atualizar produto";
                    if (error.contains("caminho_imagem") || error.contains("PGRST204")) {
                        mensagem = "Erro com o campo de imagem. Verifique as configurações do banco.";
                    }

                    Toast.makeText(AdminEditarProdutoActivity.this,
                            mensagem,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir este produto?\n\n" +
                        "⚠️ ATENÇÃO: Não será possível excluir se houver pedidos vinculados.")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void excluirProduto() {
        String idString = produtoOriginal.getId();

        if (idString == null || idString.isEmpty()) {
            Toast.makeText(this, "Erro: Produto sem ID válido", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int id = Integer.parseInt(idString);
            Log.d(TAG, "Tentando excluir produto ID: " + id);

            supabaseClient.deleteProduct(id, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean sucesso) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "✓ Produto excluído com sucesso!");
                        Toast.makeText(AdminEditarProdutoActivity.this,
                                "Produto excluído com sucesso!",
                                Toast.LENGTH_SHORT).show();

                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "❌ Erro ao excluir: " + error);

                        String titulo = "Não foi possível excluir";
                        String mensagem;

                        // Detectar erro de foreign key constraint
                        if (error.contains("23503") ||
                                error.contains("foreign key") ||
                                error.contains("still referenced") ||
                                error.contains("violates")) {

                            mensagem = "Este produto não pode ser excluído porque existem " +
                                    "pedidos vinculados a ele.\n\n" +
                                    "💡 Sugestões:\n" +
                                    "• Deixe o estoque em 0 para ocultá-lo\n" +
                                    "• Aguarde a conclusão dos pedidos pendentes";
                        } else {
                            mensagem = "Erro ao excluir produto:\n" + error;
                        }

                        new AlertDialog.Builder(AdminEditarProdutoActivity.this)
                                .setTitle(titulo)
                                .setMessage(mensagem)
                                .setPositiveButton("OK", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Log.e(TAG, "ID inválido: " + idString, e);
            Toast.makeText(this, "Erro: ID do produto inválido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageViewPreview != null) {
            imageViewPreview.setImageDrawable(null);
        }
    }
}