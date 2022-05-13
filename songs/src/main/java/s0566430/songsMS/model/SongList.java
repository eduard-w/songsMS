package s0566430.songsMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.istack.NotNull;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "songlists")
@JsonIgnoreProperties({"listId", "ownerId", "songsXmlRoot"})
public class SongList {

    @Id
    @NotNull
    @GenericGenerator(name="songlists_gen" , strategy="increment")
    @GeneratedValue(generator="songlists_gen")
    private int listId;
    @NotNull
    private String ownerId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="songlists_songs",
            joinColumns = @JoinColumn(name = "listId"),
            inverseJoinColumns = @JoinColumn(name = "songId"))
    private List<Song> songs = new ArrayList<>();

    @NotNull
    private String listName;
    @NotNull
    private boolean isPrivate;

    public SongList() {}

    public SongList(String ownerId, String listName, boolean isPrivate) {
        this.ownerId = ownerId;
        this.listName = listName;
        this.isPrivate = isPrivate;
    }

    @XmlTransient
    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    @XmlTransient
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @XmlElement(name = "songs")
    public SongsXmlRoot getSongsXmlRoot() {
        return new SongsXmlRoot(getSongs());
    }

    @XmlTransient
    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}