package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    private LinearLayout container;
    private Button btnEnviar;
    private final List<PreguntasKolb> preguntas = new ArrayList<>();
    private final android.util.SparseIntArray respuestas = new android.util.SparseIntArray();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_test);
        container = findViewById(R.id.questionsContainer);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(v -> enviar());
        cargar();
    }

    private void cargar() {
        btnEnviar.setEnabled(false); btnEnviar.setText("Cargando...");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getPreguntas().enqueue(new Callback<List<PreguntasKolb>>() {
            @Override public void onResponse(Call<List<PreguntasKolb>> c, Response<List<PreguntasKolb>> r) {
                btnEnviar.setText("Enviar respuestas");
                if (!r.isSuccessful() || r.body()==null) { toast("No se pudieron cargar"); return; }
                preguntas.clear(); preguntas.addAll(r.body()); render(); btnEnviar.setEnabled(true);
            }
            @Override public void onFailure(Call<List<PreguntasKolb>> c, Throwable t) {
                btnEnviar.setText("Enviar respuestas"); toast("Error: "+t.getMessage());
            }
        });
    }

    private void render() {
        container.removeAllViews();
        for (int i=0;i<preguntas.size();i++) {
            PreguntasKolb p = preguntas.get(i);
            TextView t1 = new TextView(this); t1.setText(p.getTitulo()!=null?p.getTitulo():p.getTipo_pregunta()); t1.setTextSize(15);
            TextView t2 = new TextView(this); t2.setText(p.getPregunta()); t2.setTextSize(14);
            RadioGroup g = new RadioGroup(this); g.setOrientation(RadioGroup.VERTICAL);
            for (int v=1; v<=4; v++){ RadioButton rb=new RadioButton(this); rb.setText(String.valueOf(v)); rb.setTag(v); g.addView(rb); }
            final int idx=i;
            g.setOnCheckedChangeListener((gr, id)->{
                RadioButton rb=gr.findViewById(id);
                if (rb!=null) respuestas.put(idx,(int)rb.getTag());
            });
            container.addView(t1); container.addView(t2); container.addView(g);
        }
    }

    private void enviar() {
        int total=preguntas.size();
        for(int i=0;i<total;i++) if(respuestas.get(i,0)==0){ toast("Responde todas antes de enviar"); return; }

        int userId = TokenManager.getUserId(this);
        if (userId<=0){ String tk=TokenManager.getToken(this); int id=TokenManager.extractUserIdFromJwt(tk); if(id>0){ userId=id; TokenManager.setUserId(this,id);} }
        if (userId<=0){ toast("Usuario inválido. Inicia sesión."); return; }

        List<KolbRequest.Respuesta> rs = new ArrayList<>(total);
        for(int i=0;i<total;i++)
            rs.add(new KolbRequest.Respuesta(preguntas.get(i).getId_pregunta_estilo_aprendizajes(), respuestas.get(i)));

        btnEnviar.setEnabled(false); btnEnviar.setText("Enviando...");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.guardarRespuestas(new KolbRequest(userId, rs)).enqueue(new Callback<KolbResponse>() {
            @Override public void onResponse(Call<KolbResponse> c, Response<KolbResponse> r) {
                btnEnviar.setEnabled(true); btnEnviar.setText("Enviar respuestas");
                if(!r.isSuccessful()||r.body()==null){ toast("Error "+r.code()); return; }
                KolbResponse k=r.body();
                String fecha=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                Intent i=new Intent(TestActivity.this, ResultActivity.class);
                i.putExtra("estilo",k.getEstiloDominante());
                i.putExtra("caracteristicas",k.getCaracteristicas());
                i.putExtra("recomendaciones",k.getRecomendaciones());
                i.putExtra("fecha",fecha); i.putExtra("nombre","");
                startActivity(i);
                toast("Estilo: "+k.getEstiloDominante());
            }
            @Override public void onFailure(Call<KolbResponse> c, Throwable t) {
                btnEnviar.setEnabled(true); btnEnviar.setText("Enviar respuestas"); toast("Fallo: "+t.getMessage());
            }
        });
    }

    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
}
