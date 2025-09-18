package com.example.myapplication;

public class AdminCardapioActivity {
private Produto produto;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_cardapio);

        produto = new Produto (getApplication());

        Button ButtonAddAC = findViewById(R.id.ButtonAddAC);
        EditText EditTextNomeProduto = findViewById(R.id.EditTextNomeProduto);
        EditText EditTextValorProduto = findViewById(R.id.EditTextValorProduto);
        EditText EditTextDetalhesProduto = findViewById(R.id.EditTextDetalhesProduto);

        ButtonAddAC.setOnClickListener(v -> {
            String nome = EditTextNomeProduto.getText().toString();
            String valorStr = EditTextValorProduto.getText().toString();
            String detalhes = EditTextDetalhesProduto.getText().toString();
        }

    }
}