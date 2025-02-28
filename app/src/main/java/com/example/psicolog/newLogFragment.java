package com.example.psicolog;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.User;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;


public class newLogFragment extends Fragment {

    Button publishButton;
    EditText postContentEditText;
    NavController navController;
    Client client;
    Account account;

    EditText etDate;
    Spinner spinnerAnxiety;

    int year, month, day;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        publishButton = view.findViewById(R.id.publishButton);
        postContentEditText = view.findViewById(R.id.postContentEditText);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicar();
            }
        });

        etDate = view.findViewById(R.id.etDate);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        String selectedDate = day + "/" + (month + 1) + "/" + year;
        etDate.setText(selectedDate);

        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Referencia al Spinner
        spinnerAnxiety = view.findViewById(R.id.anxietySpinner);

        // Lista de números del 1 al 5
        Integer[] numbers = {1, 2, 3, 4, 5};

        // Crear un adaptador para el Spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, numbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al Spinner
        spinnerAnxiety.setAdapter(adapter);

        // Manejar la selección del Spinner
        spinnerAnxiety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedNumber = (int) parent.getItemAtPosition(position);
                Toast.makeText(getActivity(), "Seleccionaste: " + selectedNumber, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Acción opcional cuando no se selecciona nada
            }
        });
    }

    private void publicar() {
        String postContent = postContentEditText.getText().toString();
        if(TextUtils.isEmpty(postContent)){
            postContentEditText.setError("Required");
            return;
        }
        publishButton.setEnabled(false);
// Obtenemos información de la cuenta del autor
        account = new Account(client);
        try {
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }
                guardarEnAppWrite(result, postContent);
            }));
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    void guardarEnAppWrite(User<Map<String, Object>> user, String content)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        // Crear instancia del servicio Databases
        Databases databases = new Databases(client);

        // Datos del documento
        Map<String, Object> data = new HashMap<>();
        data.put("comments", content);
        data.put("wellness", Integer.parseInt(spinnerAnxiety.getSelectedItem().toString()));

        String fechaFormateada = String.format("%04d%02d%02d", year, month+1, day);

        // Crear el documento
        try {
            databases.createDocument(
                    getString(R.string.APPWRITE_DATABASE_ID),
                    getString(R.string.APPWRITE_DAYLYLOGS_COLLECTION_ID),
                    fechaFormateada,
                    data,
                    new ArrayList<>(), // Permisos opcionales, como ["role:all"]
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null) {
                            Snackbar.make(requireView(), "Error: " +
                                    error.toString(), Snackbar.LENGTH_LONG).show();
                        }
                        else
                        {
                            System.out.println("Post creado:" +
                                    result.toString());
                            mainHandler.post(() ->
                            {
                                navController.popBackStack();
                            });
                        }
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    private void showDatePickerDialog() {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    year = selectedYear;
                    month = selectedMonth;
                    day = selectedDay;
                    // Mostrar la fecha seleccionada en el EditText
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}