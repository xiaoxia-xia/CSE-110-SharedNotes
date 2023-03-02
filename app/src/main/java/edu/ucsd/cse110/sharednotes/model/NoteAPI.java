package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getNote(String msg) {
        msg = msg.replace(" ", "%20");
        var body ="";

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            body = response.body().string();
            Log.i("GETNOTE", body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }

    public String postNote(Note note) {
        String msg = note.title;
        msg = msg.replace(" ", "%20");
        String jsonNote = "{" +
                "  \"content\": \"" + note.content + "\" ," +
                "  \"updated_at\": \"" + note.version + "\" ," +
                "}";
        String responseStr = "";

        RequestBody body = RequestBody.create(jsonNote, JSON);
        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + msg)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            responseStr = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseStr;
    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
}
