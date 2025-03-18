package com.example.psicolog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;


public class homeFragment extends Fragment {

    NavController navController;

    LogsAdapter adapter;

    Client client;
    Account account;
    AppViewModel appViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        navController = Navigation.findNavController(view);
        appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 navController.navigate(R.id.newLogFragment);
             }
         });

        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        adapter = new LogsAdapter();
        postsRecyclerView.setAdapter(adapter);

        obtenerLogs();
    }

    void obtenerLogs()
    {
        Databases databases = new Databases(client);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        try {
            databases.listDocuments(
                    getString(R.string.APPWRITE_DATABASE_ID), // databaseId
                    getString(R.string.APPWRITE_DAYLYLOGS_COLLECTION_ID), // collectionId
                    Arrays.asList(/*Query.Companion.isNull("parentPost"),*/  Query.Companion.orderDesc("$id"), Query.Companion.limit(50)),
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null) {
                            Snackbar.make(requireView(), "Error al obtener los posts: "
                                    + error.toString(), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println( result.toString() );
                        mainHandler.post(() -> adapter.establecerLista(result));
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    class LogViewHolder extends RecyclerView.ViewHolder
    {
        TextView dateTextView, contentTextView, anxietyTextView;
        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            anxietyTextView = itemView.findViewById(R.id.anxietyTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }

    class LogsAdapter extends RecyclerView.Adapter<LogViewHolder> {
        DocumentList<Map<String,Object>> lista = null;
        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
                viewType) {
            return new
                    LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_log, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position)
        {
            Map<String,Object> post =
                    lista.getDocuments().get(position).getData();

            String postId = post.get("$id").toString();

            String fecha = postId.substring(6,8)+"/"+postId.substring(4,6)+"/"+postId.substring(0,4);

            holder.dateTextView.setText(fecha);
            holder.anxietyTextView.setText(post.get("wellness").toString());
            holder.contentTextView.setText(post.get("comments").toString());

            int []colors = {R.color.wellness1, R.color.wellness2, R.color.wellness3, R.color.wellness4, R.color.wellness5};
            holder.anxietyTextView.setBackgroundColor(ContextCompat.getColor(getContext(), colors[Integer.parseInt(post.get("wellness").toString()) - 1]));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appViewModel.currentLog.postValue(post);
                    navController.navigate(R.id.viewLogFragment);
                }
            });
        }
        @Override
        public int getItemCount() {
            return lista == null ? 0 : lista.getDocuments().size();
        }

        public void establecerLista(DocumentList<Map<String,Object>> lista)
        {
            this.lista = lista;
            notifyDataSetChanged();
        }
    }
}