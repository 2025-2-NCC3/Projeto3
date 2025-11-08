package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminCardapioActivity extends AppCompatActivity {
    private static final String TAG = "AdminCardapioActivity";
    private static final String BUCKET_NAME = "Imagem";

    private SupabaseClient supabaseClient;
    private ImageButton btnVoltar;
    private MaterialButton btnSalvarProduto, btnCancelar, btnSelecionarImagem;
    private EditText editTextNomeProduto, editTextValorProduto, editTextDetalhesProduto, editTextEstoque, editTextImagemProduto;
    private Spinner spinnerCategoria;
    private MaterialCardView cardImagemPreview;
    private ImageView imagemPreview;

    private byte[] selectedImageBytes;
    private String uploadedImageUrl;
    private boolean isUploading = false;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cardapio);

        supabaseClient = SupabaseClient.getInstance(this);

        initializeComponents();
        setupListeners();
        setupCategoriaSpinner();
        setupImagePicker();
        createBucketIfNeeded();
    }

    private void initializeComponents() {
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSalvarProduto = findViewById(R.id.btnSalvarProduto);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem);

        editTextNomeProduto = findViewById(R.id.EditTextNomeProduto);
        editTextValorProduto = findViewById(R.id.EditTextValorProduto);
        editTextDetalhesProduto = findViewById(R.id.EditTextDetalhesProduto);
        editTextEstoque = findViewById(R.id.EditTextEstoque);
        editTextImagemProduto = findViewById(R.id.EditTextImagemProduto);

        spinnerCategoria = findViewById(R.id.SpinnerCategoria);
        cardImagemPreview = findViewById(R.id.cardImagemPreview);
        imagemPreview = findViewById(R.id.imagemPreview);
    }

    private void setupListeners() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão Salvar Produto
        btnSalvarProduto.setOnClickListener(v -> adicionarProduto());

        // Botão Cancelar
        btnCancelar.setOnClickListener(v -> {
            limparCampos();
            finish();
        });

        // Botão Selecionar Imagem
        btnSelecionarImagem.setOnClickListener(v -> selecionarImagem());
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
        String[] categorias = {"Lanches", "Bebidas", "Doces", "Marmitas", "Outros"};

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

            // Mostrar preview
            imagemPreview.setImageURI(uri);
            cardImagemPreview.setVisibility(android.view.View.VISIBLE);

            // Atualizar texto do botão
            btnSelecionarImagem.setText("IMAGEM SELECIONADA ✓");
            btnSelecionarImagem.setIconResource(R.drawable.ic_check);

            // Preencher nome do arquivo automaticamente
            String nomeArquivo = "produto_" + System.currentTimeMillis();
            editTextImagemProduto.setText(nomeArquivo);

            Toast.makeText(this,
                    "Imagem selecionada: " + (selectedImageBytes.length / 1024) + " KB",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            Toast.makeText(this, "Erro ao processar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void adicionarProduto() {
        if (!validarCampos()) {
            return;
        }

        if (selectedImageBytes != null) {
            uploadImageAndSaveProduct();
        } else {
            salvarProdutoFinal("");
        }
    }

    private void uploadImageAndSaveProduct() {
        if (isUploading) {
            Toast.makeText(this, "Upload em andamento, aguarde...", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        btnSalvarProduto.setEnabled(false);
        btnSalvarProduto.setText("FAZENDO UPLOAD...");

        String fileName = editTextImagemProduto.getText().toString().trim();
        if (fileName.isEmpty()) {
            fileName = "produto_" + System.currentTimeMillis();
        }
        fileName += ".jpg";

        supabaseClient.uploadProductImage(selectedImageBytes, fileName, BUCKET_NAME,
                new SupabaseClient.SupabaseCallback<SupabaseClient.ImageUploadResponse>() {
                    @Override
                    public void onSuccess(SupabaseClient.ImageUploadResponse response) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Upload realizado com sucesso: " + response.publicUrl);
                            uploadedImageUrl = response.publicUrl;
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
        String nome = editTextNomeProduto.getText().toString().trim();
        String valorStr = editTextValorProduto.getText().toString().trim();
        String detalhes = editTextDetalhesProduto.getText().toString().trim();
        String estoqueStr = editTextEstoque.getText().toString().trim();
        int categoriaIndex = spinnerCategoria.getSelectedItemPosition() + 1;

        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            int estoque = Integer.parseInt(estoqueStr);

            Produto novoProduto = new Produto(nome, detalhes, caminhoImagem, valor, estoque, categoriaIndex);

            if (!isUploading) {
                btnSalvarProduto.setEnabled(false);
                btnSalvarProduto.setText("SALVANDO...");
            }

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
            double valor = Double.parseDouble(editTextValorProduto.getText().toString().trim().replace(",", "."));
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
        if (!supabaseClient.isConfigured()) {
            Toast.makeText(this, "Erro: SupabaseClient não configurado", Toast.LENGTH_LONG).show();
            resetarInterface();
            return;
        }

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

        OkHttpClient client = criarOkHttpClientComSSLPermissivo();

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
                                "✅ Produto '" + produto.getNome() + "' adicionado com sucesso!",
                                Toast.LENGTH_LONG).show();

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

    private OkHttpClient criarOkHttpClientComSSLPermissivo() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar OkHttpClient SSL permissivo", e);
            return new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
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
        btnSalvarProduto.setEnabled(true);
        btnSalvarProduto.setText("SALVAR PRODUTO");
        btnSalvarProduto.setIconResource(R.drawable.ic_save);
    }

    private void limparCampos() {
        editTextNomeProduto.setText("");
        editTextValorProduto.setText("");
        editTextDetalhesProduto.setText("");
        editTextEstoque.setText("");
        editTextImagemProduto.setText("");
        spinnerCategoria.setSelection(0);

        selectedImageBytes = null;
        uploadedImageUrl = null;

        btnSelecionarImagem.setText("SELECIONAR IMAGEM");
        btnSelecionarImagem.setIconResource(R.drawable.ic_image);

        imagemPreview.setImageResource(android.R.color.transparent);
        cardImagemPreview.setVisibility(android.view.View.GONE);

        editTextNomeProduto.requestFocus();
    }
}