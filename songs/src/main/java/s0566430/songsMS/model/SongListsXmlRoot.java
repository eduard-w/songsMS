package s0566430.songsMS.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "songlists")
public class SongListsXmlRoot {
    private List<SongList> songLists;

    public SongListsXmlRoot() {
    }

    public SongListsXmlRoot(List<SongList> songLists) { this.songLists = songLists; }

    public SongListsXmlRoot(SongList songList) {
        this.songLists = new LinkedList<>();
        songLists.add(songList);
    }

    public void setSongLists(List<SongList> songLists) {
        this.songLists = songLists;
    }

    @XmlElement(name = "songlist")
    public List<SongList> getSongLists() {
        return songLists;
    }
}
