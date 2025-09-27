package com.example.zavira_movil.Home;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Question;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.VH> {

    private final List<Question> data = new ArrayList<>();
    private final Map<Integer, String> selected = new HashMap<>(); // pos -> "A"|"B"|"C"|"D"

    public void setData(List<Question> list) {
        data.clear();
        if (list != null) data.addAll(list);
        selected.clear();
        notifyDataSetChanged();
    }

    public List<com.example.zavira_movil.model.CerrarRequest.Respuesta> collectAnswers() {
        List<com.example.zavira_movil.model.CerrarRequest.Respuesta> out = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String k = selected.get(i);
            if (!TextUtils.isEmpty(k)) out.add(new com.example.zavira_movil.model.CerrarRequest.Respuesta(i + 1, k));
        }
        return out;
    }

    public int firstUnanswered() {
        for (int i = 0; i < data.size(); i++) if (TextUtils.isEmpty(selected.get(i))) return i;
        return -1;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Question q = data.get(pos);
        h.tvEnunciado.setText((q.enunciado != null ? q.enunciado : ""));

        String A = "", B = "", C = "", D = "";
        if (q.opciones != null) {
            for (Question.Option o : q.opciones) {
                if (o == null || o.key == null) continue;
                switch (o.key.toUpperCase()) {
                    case "A": A = o.text; break;
                    case "B": B = o.text; break;
                    case "C": C = o.text; break;
                    case "D": D = o.text; break;
                }
            }
        }
        h.rbA.setText(A);
        h.rbB.setText(B);
        h.rbC.setText(C);
        h.rbD.setText(D);

        h.rbA.setTag("A"); h.rbB.setTag("B"); h.rbC.setTag("C"); h.rbD.setTag("D");

        h.group.setOnCheckedChangeListener(null);
        h.group.clearCheck();
        String sel = selected.get(pos);
        if ("A".equals(sel)) h.rbA.setChecked(true);
        else if ("B".equals(sel)) h.rbB.setChecked(true);
        else if ("C".equals(sel)) h.rbC.setChecked(true);
        else if ("D".equals(sel)) h.rbD.setChecked(true);

        h.group.setOnCheckedChangeListener((g, id) -> {
            View rb = g.findViewById(id);
            if (rb != null && rb.getTag() != null) selected.put(h.getBindingAdapterPosition(), String.valueOf(rb.getTag()));
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        MaterialTextView tvEnunciado;
        RadioGroup group;
        RadioButton rbA, rbB, rbC, rbD;
        VH(@NonNull View v) {
            super(v);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            group = v.findViewById(R.id.group);
            rbA = v.findViewById(R.id.rbA);
            rbB = v.findViewById(R.id.rbB);
            rbC = v.findViewById(R.id.rbC);
            rbD = v.findViewById(R.id.rbD);
        }
    }
}
