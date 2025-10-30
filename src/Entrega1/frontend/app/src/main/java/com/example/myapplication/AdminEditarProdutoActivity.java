package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private EditText editNome, editValor, editDetalhes, editImagem, editEstoque;
    private Spinner spinnerCategoria;
    private ImageView imageViewPreview;
    private Button btnSalvar, btnExcluir, btnSelecionarImagem, btnVoltar;

    private SupabaseClient supabaseClient;
    private Produto produtoOriginal;
    private Uri novaImagemUri;

    private ActivityResultLauncher<String> selecionarImagemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_produto);

        // Inicializar componentes
        editNome = findViewById(R.id.EditTextNomeProduto);
        editValor = findViewById(R.id.EditTextValorProduto);
        editDetalhes = findViewById(R.id.EditTextDetalhesProduto);
        editImagem = findViewById(R.id.EditTextImagemProduto);
        editEstoque = findViewById(R.id.EditTextEstoque);
        spinnerCategoria = findViewById(R.id.SpinnerCategoria);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        btnSalvar = findViewById(R.id.ButtonSalvarAlteracoes);
        btnExcluir = findViewById(R.id.ButtonExcluir);
        btnSelecionarImagem = findViewById(R.id.buttonSelecionarImagem);
        btnVoltar = findViewById(R.id.botaoVoltar);

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

        // Preencher campos com dados do produto
        preencherCampos();

        // Configurar launcher para seleção de imagem
        selecionarImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        novaImagemUri = uri;
                        imageViewPreview.setImageURI(uri);
                        imageViewPreview.setVisibility(android.view.View.VISIBLE);
                    }
                }
        );

        // Botões
        btnVoltar.setOnClickListener(v -> finish());

        btnSelecionarImagem.setOnClickListener(v -> selecionarImagemLauncher.launch("image/*"));

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

        // Imagem atual
        String caminhoImagem = produtoOriginal.getCaminhoImagem();
        if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
            editImagem.setText(caminhoImagem);

            if (caminhoImagem.startsWith("http://") || caminhoImagem.startsWith("https://")) {
                Glide.with(this)
                        .load(caminhoImagem)
                        .placeholder(R.drawable.sem_imagem)
                        .error(R.drawable.sem_imagem)
                        .into(imageViewPreview);
            } else {
                imageViewPreview.setImageResource(R.drawable.sem_imagem);
            }
        }
    }

    private void salvarAlteracoes() {
        // Validar campos
        String nome = editNome.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
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

            // Criar produto atualizado
            Produto produtoAtualizado = new Produto(
                    produtoOriginal.getId(),
                    nome,
                    valor,
                    detalhes,
                    produtoOriginal.getCaminhoImagem(), // Por enquanto mantém a imagem antiga
                    estoque,
                    categoria
            );

            // Se selecionou nova imagem, fazer upload primeiro
            if (novaImagemUri != null) {
                Toast.makeText(this, "Fazendo upload da nova imagem...", Toast.LENGTH_SHORT).show();

                supabaseClient.uploadImage(novaImagemUri, new SupabaseClient.SupabaseCallback<String>() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        produtoAtualizado.setCaminhoImagem(imageUrl);
                        atualizarProdutoNoBanco(produtoAtualizado);
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminEditarProdutoActivity.this,
                                    "Erro ao fazer upload da imagem: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                atualizarProdutoNoBanco(produtoAtualizado);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarProdutoNoBanco(Produto produto) {
        supabaseClient.updateProduct(produto, new SupabaseClient.SupabaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto produtoAtualizado) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminEditarProdutoActivity.this,
                            "Produto atualizado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao atualizar produto: " + error);
                    Toast.makeText(AdminEditarProdutoActivity.this,
                            "Erro ao atualizar produto: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir este produto? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProduto() {
        supabaseClient.deleteProduct(produtoOriginal.getId(), new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean sucesso) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminEditarProdutoActivity.this,
                            "Produto excluído com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao excluir produto: " + error);
                    Toast.makeText(AdminEditarProdutoActivity.this,
                            "Erro ao excluir produto: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}