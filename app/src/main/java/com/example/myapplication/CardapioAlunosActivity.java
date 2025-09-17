package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class CardapioAlunosActivity extends AppCompatActivity {

    Button botaoVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // Função para retornar à MainActivity
        botaoVoltar = findViewById(R.id.botaoVoltar);
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Instanciando diferentes produtos para teste da lista dinâmica
        List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto(1, "Coxinha", "Coxinha recheada de frango.", "Foto da coxinha", 1.99, 10, 1, "coxinha_exemplo"));
        produtos.add(new Produto(2, "Croissant", "Croissant de presunto e queijo.","Foto do croissant", 2.99, 12, 1, "croissant_exemplo"));
        produtos.add(new Produto(3, "Brownie", "Brownie de chocolate.","Foto do brownie", 2.49, 20, 2, "brownie_exemplo"));

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("activity_cardapio.xml");

            // Encontra pelo ID o elemento "pai" onde os produtos serão adicionados
            Node container = doc.getElementById("@+id/boxLista");

            // Itera sobre o array produtos para criar o layout de cada produto
            for (Produto produto : produtos) {
                // Cria o LinearLayout principal do produto
                Element boxProduto = doc.createElement("LinearLayout");
                boxProduto.setAttribute("android:id", "@+id/boxProduto" + produto.getId());
                boxProduto.setAttribute("android:layout_width", "wrap_content");
                boxProduto.setAttribute("android:layout_height", "wrap_content");
                boxProduto.setAttribute("android:layout_marginTop", "10dp");
                boxProduto.setAttribute("android:orientation", "horizontal");

                // Cria o ImageView
                Element imagemProduto = doc.createElement("ImageView");
                imagemProduto.setAttribute("android:id", "@+id/imagemProduto" + produto.getId());
                imagemProduto.setAttribute("android:layout_width", "100dp");
                imagemProduto.setAttribute("android:layout_height", "100dp");
                imagemProduto.setAttribute("android:contentDescription", produto.getImagemDescricao());
                imagemProduto.setAttribute("android:src", "@drawable/" + produto.getCaminhoImagem());

                // Cria o LinearLayout para as informações (título e descrição)
                Element infoProduto = doc.createElement("LinearLayout");
                infoProduto.setAttribute("android:id", "@+id/infoProduto" + produto.getId());
                infoProduto.setAttribute("android:layout_width", "wrap_content");
                infoProduto.setAttribute("android:layout_height", "wrap_content");
                infoProduto.setAttribute("android:orientation", "vertical");

                // Cria o TextView do título
                Element tituloProduto = doc.createElement("TextView");
                tituloProduto.setAttribute("android:id", "@+id/tituloProduto" + produto.getId());
                tituloProduto.setAttribute("android:layout_width", "wrap_content");
                tituloProduto.setAttribute("android:layout_height", "wrap_content");
                tituloProduto.setAttribute("android:layout_marginStart", "10dp");
                tituloProduto.setAttribute("android:gravity", "center");
                tituloProduto.setAttribute("android:text", produto.getNome());
                tituloProduto.setAttribute("android:textSize", "28sp");

                // Cria o TextView da descrição
                Element descricaoProduto = doc.createElement("TextView");
                descricaoProduto.setAttribute("android:id", "@+id/descricaoProduto" + produto.getId());
                descricaoProduto.setAttribute("android:layout_width", "wrap_content");
                descricaoProduto.setAttribute("android:layout_height", "wrap_content");
                descricaoProduto.setAttribute("android:layout_marginStart", "10dp");
                descricaoProduto.setAttribute("android:gravity", "center");
                descricaoProduto.setAttribute("android:text", "R$" + produto.getPreco());
                descricaoProduto.setAttribute("android:textSize", "28sp");

                // Monta a estrutura da visualização
                infoProduto.appendChild(tituloProduto);
                infoProduto.appendChild(descricaoProduto);

                boxProduto.appendChild(imagemProduto);
                boxProduto.appendChild(infoProduto);

                // Adiciona um nó de texto para a quebra de linha e indentação antes do elemento
                container.appendChild(doc.createTextNode("\n        "));
                container.appendChild(boxProduto);
            }

            // Adiciona a quebra de linha final antes de fechar a tag do container
            container.appendChild(doc.createTextNode("\n\n    "));

            // --- 6. Escreva o conteúdo modificado de volta no arquivo XML ---
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Para manter a formatação
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("seu_layout.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        }
}