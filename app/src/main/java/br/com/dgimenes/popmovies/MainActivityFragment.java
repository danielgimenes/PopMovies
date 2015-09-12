package br.com.dgimenes.popmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.dgimenes.popmovies.model.MovieSummary;

public class MainActivityFragment extends Fragment {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static String MovieDB_API_KEY = "a75ccb1adc464aeef37492238c1165c9"; // get yours at https://www.themoviedb.org/
    private static String movieDBImagesBaseUrl = "http://image.tmdb.org/t/p/";
    private static String movieDBposterSize = "w342";
    private static String movieDBListsBaseUrl = "http://api.themoviedb.org/3/movie/";

    private ArrayAdapter<MovieSummary> adapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView postersGridView = (GridView) rootView.findViewById(R.id.posters_grid_view);
        adapter = new MoviePosterAdapter(getActivity());
        postersGridView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPosters();
    }

    private void loadPosters() {
        Log.v(LOG_TAG, "loadPosters");
        new FetchMoviePosterTask().execute();
    }

    public class MoviePosterAdapter extends ArrayAdapter<MovieSummary> {

        private static final int MOVIE_ID_TAG_KEY = 123;

        public MoviePosterAdapter(Context context) {
            super(context, R.layout.movie_poster);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.v(LOG_TAG, "getView " + Integer.valueOf(position).toString());
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.movie_poster, null);
            }

            MovieSummary movieSummary = getItem(position);

            if (movieSummary != null) {
                ImageView posterImageView = (ImageView) itemView.findViewById(R.id.poster_image_view);
                Uri imageUrl = Uri.parse(movieDBImagesBaseUrl)
                        .buildUpon()
                        .appendEncodedPath(movieDBposterSize)
                        .appendEncodedPath(movieSummary.getPosterUrl())
                        .appendQueryParameter("api_key", MovieDB_API_KEY)
                        .build();
                Picasso.with(getContext()).load(imageUrl.toString())
                        .placeholder(new ColorDrawable()).into(posterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v(LOG_TAG, "Picture downloaded and set");
                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "error downloading picture with Picasso");
                    }
                });
                posterImageView.setTag(R.id.poster_image_view, movieSummary.getId());
                posterImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String movieId = (String) v.getTag(MOVIE_ID_TAG_KEY);
                        Intent detailsActivityIntent = new Intent(
                                MainActivityFragment.this.getActivity(), DetailsActivity.class);
                        detailsActivityIntent.putExtra(DetailsActivity.MOVIE_ID_PARAM, movieId);
                        startActivity(detailsActivityIntent);
                    }
                });
            }
            return itemView;
        }
    }

    private class FetchMoviePosterTask extends AsyncTask<Void, Void, List<MovieSummary>> {

        @Override
        protected List<MovieSummary> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                String movieListName = "top_rated";
                Uri movieListUri = Uri.parse(movieDBListsBaseUrl)
                        .buildUpon()
                        .appendEncodedPath(movieListName)
                        .appendQueryParameter("api_key", MovieDB_API_KEY)
                        .build();

                URL movieListUrl = new URL(movieListUri.toString());

                urlConnection = (HttpURLConnection) movieListUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    Log.e(LOG_TAG, "Error: Stream empty");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    Log.e(LOG_TAG, "Error: Stream empty");
                    return null;
                }
                String movieListJsonStr = buffer.toString();

                return getMovieSummariesFromJsonStr(movieListJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error fetching data", e);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        private List<MovieSummary> getMovieSummariesFromJsonStr(String movieListJsonStr)
                throws JSONException {
            List<MovieSummary> movies = new ArrayList<>();
            JSONArray results = new JSONObject(movieListJsonStr).getJSONArray("results");
            for (int i = 0; i < results.length(); ++i) {
                JSONObject movie = results.getJSONObject(i);
                movies.add(new MovieSummary(movie.getString("id"), movie.getString("poster_path")));
            }
            return movies;
        }

        @Override
        protected void onPostExecute(List<MovieSummary> movieSummaries) {
            if (movieSummaries == null) {
                Toast.makeText(MainActivityFragment.this.getActivity(),
                        "Error downloading movie list :(", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.v(LOG_TAG, "movie summaries downloaded, updating adapter");
            adapter.clear();
            adapter.addAll(movieSummaries);
        }
    }
}
