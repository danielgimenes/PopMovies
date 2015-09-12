package br.com.dgimenes.popmovies;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
            Movie movie = new Movie("id", "originalTitle", "rating", "synopsis",
                    "http://image.tmdb.org/t/p/w342/yRXTVpDRBA3983C3HjoY0SO4dV6.jpg" +
                            "?api_key=a75ccb1adc464aeef37492238c1165c9",
                    "releaseDate");
            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie == null) {
                Toast.makeText(DetailsActivity.this, "Error downloading movie data :(",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ColorDrawable loadingDrawable =
                    new ColorDrawable(getResources().getColor(R.color.loading_bg_color, null));
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
            userRatingTextView.setText(movie.getId());
            synopsisTextView.setText(movie.getSynopsis());
        }
    }
}
