package br.com.dgimenes.popmovies;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.dgimenes.popmovies.model.Movie;

public class DetailsActivity extends AppCompatActivity {

    public static final String MOVIE_ID_PARAM = "MOVIE_ID";
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private TextView originalTitleTextView;
    private TextView releaseDateTextView;
    private TextView userRatingTextView;
    private ImageView posterImageView;
    private TextView synopsisTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String movieId = getIntent().getExtras().getString(MOVIE_ID_PARAM);
        originalTitleTextView = (TextView) findViewById(R.id.original_title_text_view);
        releaseDateTextView = (TextView) findViewById(R.id.release_date_text_view);
        userRatingTextView = (TextView) findViewById(R.id.user_rating_text_view);
        posterImageView = (ImageView) findViewById(R.id.poster_image_view);
        synopsisTextView = (TextView) findViewById(R.id.synopsis_text_view);
        new FetchMovieDataTask().execute(movieId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchMovieDataTask extends AsyncTask<String, Void, Movie> {
        @Override
        protected Movie doInBackground(String[] params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                String movieId = params[0];
                Uri movieUri = Uri.parse(MovieDB.MOVIE_BASE_URL)
                        .buildUpon()
                        .appendEncodedPath(movieId)
                        .appendQueryParameter(MovieDB.API_KEY_QUERYPARAM, MovieDB.API_KEY)
                        .build();

                URL movieUrl = new URL(movieUri.toString());

                urlConnection = (HttpURLConnection) movieUrl.openConnection();
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
                String movieJsonStr = buffer.toString();

                return getMovieFromJsonStr(movieJsonStr);

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

        private Movie getMovieFromJsonStr(String movieJsonStr) throws JSONException {
            JSONObject movieJsonObject = new JSONObject(movieJsonStr);
            Uri posterUri = Uri.parse(MovieDB.IMAGES_BASE_URL)
                    .buildUpon()
                    .appendEncodedPath(MovieDB.POSTER_SIZE)
                    .appendEncodedPath(movieJsonObject.getString("poster_path"))
                    .appendQueryParameter(MovieDB.API_KEY_QUERYPARAM, MovieDB.API_KEY)
                    .build();
            Movie movie = new Movie(movieJsonObject.getString("id"),
                    movieJsonObject.getString("original_title"),
                    movieJsonObject.getString("vote_average"),
                    movieJsonObject.getString("overview"),
                    posterUri.toString(),
                    movieJsonObject.getString("release_date")
            );
            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie == null) {
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.error_downloading_movie_data),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ColorDrawable loadingDrawable = new ColorDrawable(
                    ContextCompat.getColor(DetailsActivity.this, R.color.loading_bg_color));
            Picasso.with(DetailsActivity.this).load(movie.getPosterUrl())
                    .placeholder(loadingDrawable)
                    .into(posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Log.e(LOG_TAG,
                                    getResources().getString(R.string.error_downloading_picture));
                        }
                    });
            originalTitleTextView.setText(movie.getOriginalTitle());
            releaseDateTextView.setText(movie.getReleaseDate());
            userRatingTextView.setText(movie.getRating());
            synopsisTextView.setText(movie.getSynopsis());
        }
    }
}
