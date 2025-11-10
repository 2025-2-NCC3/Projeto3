package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import okhttp3.*;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manager para gerenciar pagamentos PIX
 * Integração com Mercado Pago API
 */
public class PixPagamentoManager {

    private static final String TAG = "PixPagamentoManager";
    private static PixPagamentoManager instance;
    private Context context;
    private OkHttpClient client;
    private Gson gson;
    private SessionManager sessionManager;


    //Chave real

     private static final String MERCADO_PAGO_ACCESS_TOKEN = "APP_USR-8207515989216625-110619-1f94f06ac21a47d5e6beb38481f0e6bb-2043562450";
    //Chave Teste
    // private static final String MERCADO_PAGO_ACCESS_TOKEN = "TEST-8207515989216625-110619-ae4fca109e7e92522ba0d1ec50976e2f-2043562450";
    private static final String MERCADO_PAGO_BASE_URL = "https://api.mercadopago.com/v1";

    private PixPagamentoManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.sessionManager = SessionManager.getInstance(context);

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized PixPagamentoManager getInstance(Context context) {
        if (instance == null) {
            instance = new PixPagamentoManager(context);
        }
        return instance;
    }

    /**
     * Cria um pagamento PIX
     */
    public void criarPagamentoPix(Pedido pedido, PagamentoCallback callback) {
        new Thread(() -> {
            try {
                // 🔧 CORREÇÃO: Pegar email do usuário logado
                String emailPagador = sessionManager.getUserEmail();

                // Validação do email
                if (emailPagador == null || emailPagador.isEmpty()) {
                    callback.onError("Erro: Usuário não está autenticado");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailPagador).matches()) {
                    callback.onError("Erro: Email do usuário inválido");
                    return;
                }

                Log.d(TAG, "Email do pagador: " + emailPagador);

                // Criar payload para Mercado Pago
                JSONObject payload = new JSONObject();
                payload.put("transaction_amount", pedido.getTotal());
                payload.put("description", "Pedido #" + pedido.getCode());
                payload.put("payment_method_id", "pix");

                // Informações do pagador - AGORA COM EMAIL REAL
                JSONObject payer = new JSONObject();
                payer.put("email", emailPagador);
                payload.put("payer", payer);

                // ID único para idempotência
                String idempotencyKey = UUID.randomUUID().toString();

                RequestBody body = RequestBody.create(
                        payload.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(MERCADO_PAGO_BASE_URL + "/payments")
                        .addHeader("Authorization", "Bearer " + MERCADO_PAGO_ACCESS_TOKEN)
                        .addHeader("X-Idempotency-Key", idempotencyKey)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    PagamentoPix pagamento = new PagamentoPix();
                    pagamento.setId(jsonResponse.getString("id"));
                    pagamento.setPedidoId(pedido.getId());
                    pagamento.setValor(pedido.getTotal());
                    pagamento.setTransactionId(jsonResponse.getString("id"));
                    pagamento.setStatus("PENDING");

                    // Extrair QR Code
                    if (jsonResponse.has("point_of_interaction")) {
                        JSONObject pointOfInteraction = jsonResponse.getJSONObject("point_of_interaction");
                        if (pointOfInteraction.has("transaction_data")) {
                            JSONObject transactionData = pointOfInteraction.getJSONObject("transaction_data");

                            if (transactionData.has("qr_code")) {
                                pagamento.setQrCodeTexto(transactionData.getString("qr_code"));
                            }

                            if (transactionData.has("qr_code_base64")) {
                                pagamento.setQrCode(transactionData.getString("qr_code_base64"));
                            }
                        }
                    }

                    callback.onSuccess(pagamento);
                } else {
                    callback.onError("Erro ao criar pagamento: " + response.message());
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro ao criar pagamento PIX", e);
                callback.onError("Erro: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Verifica o status de um pagamento
     */
    public void verificarStatusPagamento(String paymentId, PagamentoCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(MERCADO_PAGO_BASE_URL + "/payments/" + paymentId)
                        .addHeader("Authorization", "Bearer " + MERCADO_PAGO_ACCESS_TOKEN)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.getString("status");

                    PagamentoPix pagamento = new PagamentoPix();
                    pagamento.setId(paymentId);
                    pagamento.setTransactionId(paymentId);

                    switch (status) {
                        case "approved":
                            pagamento.setStatus("APPROVED");
                            break;
                        case "rejected":
                        case "cancelled":
                            pagamento.setStatus("CANCELLED");
                            break;
                        default:
                            pagamento.setStatus("PENDING");
                            break;
                    }

                    callback.onSuccess(pagamento);
                } else {
                    callback.onError("Erro ao verificar status: " + response.message());
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro ao verificar status", e);
                callback.onError("Erro: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Gera um QR Code a partir de um texto
     */
    public Bitmap gerarQRCode(String texto, int tamanho) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(texto, BarcodeFormat.QR_CODE, tamanho, tamanho);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(matrix);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao gerar QR Code", e);
            return null;
        }
    }

    /**
     * Callback para operações de pagamento
     */
    public interface PagamentoCallback {
        void onSuccess(PagamentoPix pagamento);
        void onError(String erro);
    }
}
