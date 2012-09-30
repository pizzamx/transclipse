/**
 * @author pizza(pizzamx@gmail.com)
 * ===============
 * In God We Trust
 * ===============
 * 2007-3-7 下午03:54:22
 *
 */
package subtitle.model;

import subtitle.utils.TimelineHelper;

public class SubtitleUnit {
    int id;

    String timeline, shortTimeline, original, translation;

    int duration;

    Moment start, end;

    public SubtitleUnit() {
        translation = "";
    }
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimeline() {
        return this.timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
        String[] t = TimelineHelper.split( timeline );
        start = new Moment( t[0] );
        end = new Moment( t[1] );
        duration = TimelineHelper.getDistance( end, start );
        shortTimeline = TimelineHelper.generateShortTimeline( start, end );
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((this.timeline == null) ? 0 : this.timeline.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SubtitleUnit other = (SubtitleUnit)obj;
        if (this.timeline == null) {
            if (other.timeline != null)
                return false;
        } else if (!this.timeline.equals( other.timeline ))
            return false;
        return true;
    }

    public String getOriginal() {
        return this.original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getTranslation() {
        return this.translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Moment getEnd() {
        return end;
    }

    public void setEnd(Moment end) {
        this.end = end;
    }

    public Moment getStart() {
        return start;
    }

    public void setStart(Moment start) {
        this.start = start;
    }

    public String getShortTimeline() {
        return shortTimeline;
    }
    /**
     * 修改了start或者end的时候调用
     */
    public void refresh() {
        shortTimeline = TimelineHelper.generateShortTimeline( start, end );
        timeline = TimelineHelper.generateTimeline( start, end );
    }
}
