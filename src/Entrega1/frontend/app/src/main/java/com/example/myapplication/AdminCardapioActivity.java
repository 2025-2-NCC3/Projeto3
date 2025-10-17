package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminCardapioActivity extends AppCompatActivity {
    private static final String TAG = "AdminCardapioActivity";
    // CORREÇÃO: Usar o nome correto do bucket
    private static final String BUCKET_NAME = "Imagem";

    // Componentes originais
    private SupabaseClient supabaseClient;
    private Button buttonAddAC, botaoVoltar;
    private EditText editTextNomeProduto, editTextValorProduto, editTextDetalhesProduto, editTextEstoque;
    private Spinner spinnerCategoria;

    // Componentes para imagem
    private Button buttonSelecionarImagem;
    private ImageView imageViewPreview;

    // Controle de upload
    private byte[] selectedImageBytes;
    private String uploadedImageUrl;
    private boolean isUploading = false;

    // ActivityResultLauncher para seleção de imagem
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_cardapio);

        // Inicializar SupabaseClient
        supabaseClient = SupabaseClient.getInstance(this);

        // Inicializar componentes
        initializeComponents();

        // Configurar listeners
        setupListeners();

        // Configurar spinner de categorias
        setupCategoriaSpinner();

        // Configurar seleção de imagem
        setupImagePicker();

        // Criar bucket se necessário
        createBucketIfNeeded();
    }

    private void initializeComponents() {
        // Componentes originais
        buttonAddAC = findViewById(R.id.ButtonAddAC);
        botaoVoltar = findViewById(R.id.botaoVoltar);
        editTextNomeProduto = findViewById(R.id.EditTextNomeProduto);
        editTextValorProduto = findViewById(R.id.EditTextValorProduto);
        editTextDetalhesProduto = findViewById(R.id.EditTextDetalhesProduto);
        editTextEstoque = findViewById(R.id.EditTextEstoque);
        spinnerCategoria = findViewById(R.id.SpinnerCategoria);

        // Componentes para imagem
        try {
            buttonSelecionarImagem = findViewById(R.id.buttonSelecionarImagem);
            imageViewPreview = findViewById(R.id.imageViewPreview);
        } catch (Exception e) {
            Log.w(TAG, "Componentes de imagem não encontrados no layout");
        }
    }

    private void setupListeners() {
        // Botão para adicionar produto
        buttonAddAC.setOnClickListener(v -> adicionarProduto());

        // Botão voltar
        botaoVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(AdminCardapioActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Botão para selecionar imagem
        if (buttonSelecionarImagem != null) {
            buttonSelecionarImagem.setOnClickListener(v -> selecionarImagem());
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processarImagemSelecionada(uri);
                    }
                }
        );
    }

    private void setupCategoriaSpinner() {
        String[] categorias = {"Bebidas", "Pratos Principais", "Sobremesas", "Entradas", "Lanches", "Outros"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategoria.setAdapter(adapter);
    }

    private void createBucketIfNeeded() {
        supabaseClient.createBucketIfNotExists(BUCKET_NAME, new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean response) {
                Log.d(TAG, "Bucket " + BUCKET_NAME + " verificado/criado");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao criar bucket: " + error);
            }
        });
    }

    private void selecionarImagem() {
        if (isUploading) {
            Toast.makeText(this, "Aguarde o upload atual terminar", Toast.LENGTH_SHORT).show();
            return;
        }

        imagePickerLauncher.launch("image/*");
    }

    private void processarImagemSelecionada(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "Erro ao abrir imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            selectedImageBytes = outputStream.toByteArray();
            inputStream.close();
            outputStream.close();

            // Atualizar interface
            if (buttonSelecionarImagem != null) {
                buttonSelecionarImagem.setText("Imagem Selecionada ✓");
            }

            if (imageViewPreview != null) {
                imageViewPreview.setImageURI(uri);
            }

            Toast.makeText(this,
                    "Imagem selecionada: " + (selectedImageBytes.length / 1024) + " KB",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            Toast.makeText(this, "Erro ao processar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void adicionarProduto() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        // Se há imagem selecionada, fazer upload primeiro
        if (selectedImageBytes != null) {
            uploadImageAndSaveProduct();
        } else {
            // Salvar produto sem imagem
            salvarProdutoFinal("");
        }
    }

    private void uploadImageAndSaveProduct() {
        if (isUploading) {
            Toast.makeText(this, "Upload em andamento, aguarde...", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        buttonAddAC.setEnabled(false);
        buttonAddAC.setText("Fazendo upload da imagem...");

        String fileName = "produto_" + System.currentTimeMillis() + ".jpg";

        supabaseClient.uploadProductImage(selectedImageBytes, fileName, BUCKET_NAME,
                new SupabaseClient.SupabaseCallback<SupabaseClient.ImageUploadResponse>() {
                    @Override
                    public void onSuccess(SupabaseClient.ImageUploadResponse response) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Upload realizado com sucesso: " + response.publicUrl);
                            uploadedImageUrl = response.publicUrl;

                            // Agora salvar o produto com a URL da imagem
                            salvarProdutoFinal(uploadedImageUrl);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Erro no upload da imagem: " + error);
                            Toast.makeText(AdminCardapioActivity.this,
                                    "Erro no upload da imagem: " + error,
                                    Toast.LENGTH_LONG).show();

                            resetarInterface();
                        });
                    }
                });
    }

    private void salvarProdutoFinal(String caminhoImagem) {
        // Obter valores dos campos
        String nome = editTextNomeProduto.getText().toString().trim();
        String valorStr = editTextValorProduto.getText().toString().trim();
        String detalhes = editTextDetalhesProduto.getText().toString().trim();
        String estoqueStr = editTextEstoque.getText().toString().trim();
        int categoriaIndex = spinnerCategoria.getSelectedItemPosition() + 1;

        try {
            double valor = Double.parseDouble(valorStr);
            int estoque = Integer.parseInt(estoqueStr);

            // Criar novo produto
            Produto novoProduto = new Produto(nome, detalhes, caminhoImagem, valor, estoque, categoriaIndex);

            if (!isUploading) {
                buttonAddAC.setEnabled(false);
                buttonAddAC.setText("Salvando produto...");
            }

            // Inserir produto no Supabase
            inserirProdutoNoSupabase(novoProduto);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor ou estoque inválido", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro ao converter valor ou estoque", e);

            resetarInterface();
        }
    }

    private boolean validarCampos() {
        if (editTextNomeProduto.getText().toString().trim().isEmpty()) {
            editTextNomeProduto.setError("Nome é obrigatório");
            editTextNomeProduto.requestFocus();
            return false;
        }

        if (editTextValorProduto.getText().toString().trim().isEmpty()) {
            editTextValorProduto.setError("Valor é obrigatório");
            editTextValorProduto.requestFocus();
            return false;
        }

        if (editTextDetalhesProduto.getText().toString().trim().isEmpty()) {
            editTextDetalhesProduto.setError("Descrição é obrigatória");
            editTextDetalhesProduto.requestFocus();
            return false;
        }

        if (editTextEstoque.getText().toString().trim().isEmpty()) {
            editTextEstoque.setError("Estoque é obrigatório");
            editTextEstoque.requestFocus();
            return false;
        }

        try {
            double valor = Double.parseDouble(editTextValorProduto.getText().toString().trim());
            if (valor <= 0) {
                editTextValorProduto.setError("Valor deve ser maior que zero");
                editTextValorProduto.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editTextValorProduto.setError("Valor inválido");
            editTextValorProduto.requestFocus();
            return false;
        }

        try {
            int estoque = Integer.parseInt(editTextEstoque.getText().toString().trim());
            if (estoque < 0) {
                editTextEstoque.setError("Estoque não pode ser negativo");
                editTextEstoque.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editTextEstoque.setError("Estoque inválido");
            editTextEstoque.requestFocus();
            return false;
        }

        return true;
    }

    private void inserirProdutoNoSupabase(Produto produto) {
        Toast.makeText(this, "Inserindo produto no banco...", Toast.LENGTH_SHORT).show();

        if (!supabaseClient.isConfigured()) {
            Toast.makeText(this, "Erro: SupabaseClient não configurado", Toast.LENGTH_LONG).show();
            resetarInterface();
            return;
        }

        // Criar JSON manualmente
        String json = createProductJson(produto);
        Log.d(TAG, "JSON a ser enviado: " + json);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/produtos")
                .post(body)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Erro na conexão ao inserir produto", e);
                    Toast.makeText(AdminCardapioActivity.this,
                            "Erro de conexão: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    resetarInterface();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    Log.d(TAG, "Código da resposta: " + response.code());
                    Log.d(TAG, "Corpo da resposta: " + responseBody);

                    if (response.isSuccessful()) {
                        Toast.makeText(AdminCardapioActivity.this,
                                "Produto '" + produto.getNome() + "' adicionado com sucesso!",
                                Toast.LENGTH_LONG).show();

                        // Limpar campos após sucesso
                        limparCampos();
                        resetarInterface();

                        Log.i(TAG, "Produto inserido com sucesso: " + produto.getNome());
                    } else {
                        String errorMsg = "Erro ao adicionar produto";
                        if (response.code() == 401) {
                            errorMsg = "Erro de autenticação. Verifique as configurações do Supabase.";
                        } else if (response.code() == 400) {
                            errorMsg = "Dados inválidos. Verifique os campos.";
                        } else if (response.code() == 409) {
                            errorMsg = "Produto já existe ou conflito de dados.";
                        }

                        Toast.makeText(AdminCardapioActivity.this,
                                errorMsg + " (Código: " + response.code() + ")",
                                Toast.LENGTH_LONG).show();

                        Log.e(TAG, "Erro HTTP: " + response.code() + " - " + responseBody);
                        resetarInterface();
                    }
                });
            }
        });
    }

    private String createProductJson(Produto produto) {
        String nome = produto.getNome().replace("\"", "\\\"").replace("\n", "\\n");
        String descricao = produto.getDescricao().replace("\"", "\\\"").replace("\n", "\\n");
        String caminhoImagem = produto.getCaminhoImagem().replace("\"", "\\\"");

        return String.format(Locale.US,
                "{\"nome\":\"%s\",\"descricao\":\"%s\",\"caminhoImagem\":\"%s\",\"preco\":%.2f,\"estoque\":%d,\"categoria\":%d}",
                nome, descricao, caminhoImagem, produto.getPreco(), produto.getEstoque(), produto.getCategoria()
        );
    }

    private void resetarInterface() {
        isUploading = false;
        buttonAddAC.setEnabled(true);
        buttonAddAC.setText("Adicionar Produto");

        // Limpar dados da imagem
        selectedImageBytes = null;
        uploadedImageUrl = null;

        if (buttonSelecionarImagem != null) {
            buttonSelecionarImagem.setText("Selecionar Imagem");
        }
    }

    private void limparCampos() {
        editTextNomeProduto.setText("");
        editTextValorProduto.setText("");
        editTextDetalhesProduto.setText("");
        editTextEstoque.setText("");
        spinnerCategoria.setSelection(0);

        // Limpar imagem
        selectedImageBytes = null;
        uploadedImageUrl = null;

        if (buttonSelecionarImagem != null) {
            buttonSelecionarImagem.setText("Selecionar Imagem");
        }

        if (imageViewPreview != null) {
            imageViewPreview.setImageResource(android.R.color.transparent);
        }

        // Focar no primeiro campo
        editTextNomeProduto.requestFocus();
    }
}