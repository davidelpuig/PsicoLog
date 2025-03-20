package com.example.psicolog;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.services.Databases;


public class viewLogFragment extends Fragment {

    TextView date;
    TextView wellness;
    TextView sleepTime;
    TextView sex;
    TextView logContent;
    Toolbar toolbar;
    Button delete;

    AppViewModel viewModel;
    String logId;

    Client client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID)); // Your project ID

        toolbar = view.getRootView().findViewById(R.id.toolbar);
        date = view.findViewById(R.id.dateTextView);
        wellness = view.findViewById(R.id.wellnessTextView);
        sleepTime = view.findViewById(R.id.sleepTimeTextView);
        sex = view.findViewById(R.id.sexTextView);
        logContent = view.findViewById(R.id.logContentTextView);
        delete = view.findViewById(R.id.deleteButton);

        toolbar = view.getRootView().findViewById(R.id.toolbar);
        toolbar.setTitle("Log diario");

        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);

        viewModel.currentLog.observe(getViewLifecycleOwner(), new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                String dateStr = stringObjectMap.get("$id").toString();
                logId = dateStr;

                Calendar day = Calendar.getInstance();
                day.set(Integer.parseInt(dateStr.substring(0,4)), Integer.parseInt(dateStr.substring(4,6)), Integer.parseInt(dateStr.substring(6,8)));

                String[] dias_semana = getResources().getStringArray(R.array.dias_semana);
                String[] meses = getResources().getStringArray(R.array.meses);
                date.setText(dias_semana[day.get(Calendar.DAY_OF_WEEK)-1]+", "+day.get(Calendar.DAY_OF_MONTH)+" de "+meses[day.get(Calendar.MONTH)]+" de "+day.get(Calendar.YEAR));
                wellness.setText("Nivel de bienestar: "+stringObjectMap.get("wellness").toString());
                int []colors = getResources().getIntArray(R.array.wellness_colors);
                wellness.setBackgroundColor(colors[Integer.parseInt(stringObjectMap.get("wellness").toString()) - 1]);
                if(stringObjectMap.get("sleep_time") != null)
                    sleepTime.setText("Horas de sueño: "+stringObjectMap.get("sleep_time").toString());
                else
                    sleepTime.setText("Horas de sueño: ?");
                if(stringObjectMap.get("sexual_activity") != null)
                    sex.setText("Actividad sexual: "+(stringObjectMap.get("sexual_activity").equals(true) ? "Sí" : "No"));
                else
                    sex.setText("Actividad sexual: ?");
                logContent.setText(stringObjectMap.get("comments").toString());
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirmación")
                        .setMessage("¿Estás seguro de borrar el log?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            // Acción al pulsar "Sí"

                            Databases databases = new Databases(client);
                            Handler mainHandler = new Handler(Looper.getMainLooper());

                            databases.deleteDocument(
                                    getString(R.string.APPWRITE_DATABASE_ID), // databaseId
                                    getString(R.string.APPWRITE_DAYLYLOGS_COLLECTION_ID), // collectionId
                                    logId,
                                    new CoroutineCallback<>((result, error) -> {
                                        if (error != null) {
                                            Snackbar.make(requireView(), "Error al borrar el post: " + error.toString(), Snackbar.LENGTH_LONG).show();
                                            return;
                                        }

                                        System.out.println( result.toString() );

                                        mainHandler.post(() -> { Navigation.findNavController(view).popBackStack(); });

                                    })
                            );
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Acción al pulsar "No"
                            System.out.println("Se canceló la acción.");
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}