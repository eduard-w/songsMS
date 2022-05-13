package s0566430.songsMS.model;

import com.sun.istack.NotNull;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @NotNull
    @GenericGenerator(name="songs_gen" , strategy="increment")
    @GeneratedValue(generator="songs_gen")
    @Column(name = "songid")
    private int id;

    @ManyToMany(mappedBy = "songs")
    private List<SongList> containingLists;

    @NotNull
    private String title;
    private String artist, label;
    private int released;

    public Song() {
    }

    public Song(String title, String artist, String label, int released) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public boolean hasBeenFullyInitialized() {
        return title!=null && artist!=null && label!=null && released!=0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getReleased() {
        return released;
    }

    public void setReleased(int released) {
        this.released = released;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return id == song.id &&
                released == song.released &&
                title.equals(song.title) &&
                artist.equals(song.artist) &&
                label.equals(song.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, artist, label, released);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", label='" + label + '\'' +
                ", released=" + released +
                '}';
    }
}
