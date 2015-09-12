package br.com.dgimenes.popmovies.model;

public class MovieSummary {
    private String id;
    private String posterUrl;

    public MovieSummary(String id, String posterUrl) {
        this.id = id;
        this.posterUrl = posterUrl;
    }

    public String getId() {
        return id;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieSummary that = (MovieSummary) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
