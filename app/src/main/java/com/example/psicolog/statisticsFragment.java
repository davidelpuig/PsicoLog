package com.example.psicolog;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;

import io.appwrite.Client;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.services.Databases;


public class statisticsFragment extends Fragment {


    Button refreshButton;
    EditText startDate, endDate;

    Client client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));

        startDate = view.findViewById(R.id.etStartDate);
        endDate = view.findViewById(R.id.etEndDate);
        refreshButton = view.findViewById(R.id.refreshButton);

        startDate.setOnClickListener(v -> showDatePickerDialog(startDate));
        endDate.setOnClickListener(v -> showDatePickerDialog(endDate));

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String date1 = startDate.getText().toString();
                String date2 = endDate.getText().toString();

                String[] fields1 = date1.split("/");
                String[] fields2 = date2.split("/");

                LocalDate start = LocalDate.of(Integer.parseInt(fields1[2]), Integer.parseInt(fields1[1]), Integer.parseInt(fields1[0]));
                LocalDate end = LocalDate.of(Integer.parseInt(fields2[2]), Integer.parseInt(fields2[1]), Integer.parseInt(fields2[0]));

                if(end.isAfter(start))
                {
                    consultarLogs(fields1[2]+fields1[1]+fields1[0], fields2[2]+fields2[1]+fields2[0]);
                }
                else
                {
                    // Mostrar el Snackbar con mensaje de error
                    Snackbar snackbar = Snackbar.make(view, "La fecha de fin debe ser posterior a la de inicio", Snackbar.LENGTH_LONG);
                    // Mostrar el Snackbar
                    snackbar.show();
                }


            }
        });

    }

    private void showDatePickerDialog(EditText dialog) {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Mostrar la fecha seleccionada en el EditText
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dialog.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    void consultarLogs(String start, String end)
    {
        Databases databases = new Databases(client);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        try {
            databases.listDocuments(
                    getString(R.string.APPWRITE_DATABASE_ID), // databaseId
                    getString(R.string.APPWRITE_DAYLYLOGS_COLLECTION_ID), // collectionId
                    Arrays.asList(/*Query.Companion.isNull("parentPost"),*/  Query.Companion.orderDesc("$id"),
                            Query.Companion.between("$id", start, end),
                            Query.Companion.limit(50)),
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null) {
                            Snackbar.make(requireView(), "Error al obtener los logs: "
                                    + error.toString(), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println( result.toString() );
                        //mainHandler.post(() -> adapter.establecerLista(result));
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }
}