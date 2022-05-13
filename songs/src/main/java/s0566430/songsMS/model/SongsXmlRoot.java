package s0566430.songsMS.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "songs")
public class SongsXmlRoot {

    public SongsXmlRoot() {
    }

    public SongsXmlRoot(List<Song> songs) {
        this.songs = songs;
    }

    public SongsXmlRoot(Song song) {
        this.songs = new LinkedList<>();
        songs.add(song);
    }

    private List<Song> songs;

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @XmlElement(name = "song")
    public List<Song> getSongs() {
        return songs;
    }
}
