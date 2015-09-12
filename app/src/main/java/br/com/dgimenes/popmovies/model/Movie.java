package br.com.dgimenes.popmovies.model;

public class Movie {
    private String id;
    private String originalTitle;
    private String rating;
    private String synopsis;
    private String posterUrl;
    private String releaseDate;

    public Movie(String id, String originalTitle, String rating, String synopsis, String posterUrl, String releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.rating = rating;
        this.synopsis = synopsis;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
    }

    public String getId() {
        return id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getRating() {
        return rating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return !(id != null ? !id.equals(movie.id) : movie.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
