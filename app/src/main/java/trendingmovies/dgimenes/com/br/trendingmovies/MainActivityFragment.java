package trendingmovies.dgimenes.com.br.trendingmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends Fragment {

    private static String MovieDB_API_KEY = "a75ccb1adc464aeef37492238c1165c9";
    private static String movieDBBaseUrl = "http://image.tmdb.org/t/p/";
    private static String movieDBposterSize = "w185";

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView postersGridView = (GridView) rootView.findViewById(R.id.postersGridView);
        List<String> posterUrls = new ArrayList<>();
        posterUrls.add("/yRXTVpDRBA3983C3HjoY0SO4dV6.jpg");
        posterUrls.add("/lIv1QinFqz4dlp5U4lQ6HaiskOZ.jpg");
        posterUrls.add("/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg");
        posterUrls.add("/3TpMBcAYH4cxCw5WoRacWodMTCG.jpg");
        ListAdapter adapter = new MoviePosterAdapter(getActivity(), posterUrls.toArray(new String[] {}));
        postersGridView.setAdapter(adapter);
        return rootView;
    }

    private class MoviePosterAdapter extends ArrayAdapter<String> {

        public MoviePosterAdapter(Context context, String[] objects) {
            super(context, R.layout.movie_poster, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.movie_poster, null);
            }

            String posterUrl = getItem(position);

            if (posterUrl != null) {
                ImageView posterImageView = (ImageView) itemView.findViewById(R.id.posterImageView);
                String imageUrl = movieDBBaseUrl + movieDBposterSize + posterUrl + "?api_key=" + MovieDB_API_KEY;
                Picasso.with(getContext()).load(imageUrl).into(posterImageView);
            }

            return itemView;
        }
    }
}
