package com.example.psicolog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Map;


public class viewLogFragment extends Fragment {

    TextView date;
    TextView wellness;
    TextView sleepTime;
    TextView sex;
    TextView logContent;

    AppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        date = view.findViewById(R.id.dateTextView);
        wellness = view.findViewById(R.id.wellnessTextView);
        sleepTime = view.findViewById(R.id.sleepTimeTextView);
        sex = view.findViewById(R.id.sexTextView);
        logContent = view.findViewById(R.id.logContentTextView);

        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);

        viewModel.currentLog.observe(getViewLifecycleOwner(), new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                String dateStr = stringObjectMap.get("$id").toString();

                Calendar day = Calendar.getInstance();
                day.set(Integer.parseInt(dateStr.substring(0,4)), Integer.parseInt(dateStr.substring(4,6)), Integer.parseInt(dateStr.substring(6,8)));

                String[] dias_semana={ "Jueves", "Viernes", "Sábado", "Domingo", "Lunes", "Martes", "Miércoles" };
                date.setText(dias_semana[day.get(Calendar.DAY_OF_WEEK)-1]+", "+day.get(Calendar.DAY_OF_MONTH)+" de "+day.get(Calendar.MONTH)+" de "+day.get(Calendar.YEAR));
                wellness.setText("Nivel de bienestar: "+stringObjectMap.get("wellness").toString());
                int []colors = {R.color.wellness1, R.color.wellness2, R.color.wellness3, R.color.wellness4, R.color.wellness5};
                wellness.setBackgroundColor(ContextCompat.getColor(getContext(), colors[Integer.parseInt(stringObjectMap.get("wellness").toString()) - 1]));
                sleepTime.setText("Horas de sueño: "+stringObjectMap.get("sleep_time").toString());
                sex.setText("Actividad sexual: "+stringObjectMap.get("sexual_activity").toString());
                logContent.setText(stringObjectMap.get("comments").toString());
            }
        });
    }
}