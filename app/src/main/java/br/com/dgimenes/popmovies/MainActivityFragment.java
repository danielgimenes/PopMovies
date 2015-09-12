package br.com.dgimenes.popmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        new FetchMoviePosterTask().execute(sortOrder);
    }

    public class MoviePosterAdapter extends ArrayAdapter<MovieSummary> {

        public MoviePosterAdapter(Context context) {
            super(context, R.layout.movie_poster);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.movie_poster, null);
            }

            MovieSummary movieSummary = getItem(position);

            if (movieSummary != null) {
                ImageView posterImageView = (ImageView) itemView.findViewById(R.id.poster_image_view);
                Uri imageUrl = Uri.parse(MovieDB.IMAGES_BASE_URL)
                        .buildUpon()
                        .appendEncodedPath(MovieDB.POSTER_SIZE)
                        .appendEncodedPath(movieSummary.getPosterUrl())
                        .appendQueryParameter(MovieDB.API_KEY_QUERYPARAM, MovieDB.API_KEY)
                        .build();
                ColorDrawable loadingDrawable =
                        new ColorDrawable(getResources().getColor(R.color.loading_bg_color, null));
                Picasso.with(getContext()).load(imageUrl.toString()).placeholder(loadingDrawable)
                        .into(posterImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Log.e(LOG_TAG, getResources()
                                        .getString(R.string.error_downloading_picture));
                            }
                        });
                posterImageView.setTag(R.id.poster_image_view, movieSummary.getId());
                posterImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String movieId = (String) v.getTag(R.id.poster_image_view);
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

    private class FetchMoviePosterTask extends AsyncTask<String, Void, List<MovieSummary>> {

        @Override
        protected List<MovieSummary> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                String sortOrder = params[0];
                Uri movieListUri = Uri.parse(MovieDB.LISTS_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(MovieDB.SORT_ORDER_QUERYPARAM, sortOrder)
                        .appendQueryParameter(MovieDB.API_KEY_QUERYPARAM, MovieDB.API_KEY)
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
                        return null;
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
                        getResources().getString(R.string.error_downloading_movie_list),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.clear();
            adapter.addAll(movieSummaries);
        }
    }
}
