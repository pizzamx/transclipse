/**
 * Created on 2007-3-18
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.model;

import javax.print.attribute.standard.MediaSize.Other;

public class Moment implements Comparable {
    int hour, min, second, millisecond;

    // 00:43:59,234
    public Moment(String string) {
        String[] t = string.split( "," );
        millisecond = Integer.parseInt( t[1] );
        t = t[0].split( ":" );
        hour = Integer.parseInt( t[0] );
        min = Integer.parseInt( t[1] );
        second = Integer.parseInt( t[2] );
    }
    public Moment(int time) {
        int t = time % 1000;
        millisecond = t;
        t = (time = (time - t) / 1000) % 60;
        second = t;
        t = (time = (time - t) / 60) % 60;
        min = t;
        t = (time = (time - t) / 60) % 24;
        hour = t;
    }
    public Moment(int a, int b, int c, int d) {
        hour = a;
        min = b;
        second = c;
        millisecond = d;
    }

    public Moment(Moment oldMoment, int difference) {
        this( oldMoment.toInt() + difference );
    }
    public int getHour() {
        return hour;
    }

    public int getMillisecond() {
        return millisecond;
    }

    public int getMin() {
        return min;
    }

    public int getSecond() {
        return second;
    }
    public int toInt() {
        return hour * 60 * 60 * 1000 + min * 60 * 1000 + second * 1000 + millisecond;
    }
    public static void main(String[] args) {
        Moment moment = new Moment( "15:43:59,234" );
        System.out.println( moment.getHour() + ", " + moment.getMin() + ", " + moment.getSecond() + ", " + moment.getMillisecond() );
        System.out.println( moment.toInt() );
        moment = new Moment( moment.toInt() );
        System.out.println( moment.getHour() + ", " + moment.getMin() + ", " + moment.getSecond() + ", " + moment.getMillisecond() );
        System.out.println( moment.toInt() );
    }
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + hour;
        result = PRIME * result + millisecond;
        result = PRIME * result + min;
        result = PRIME * result + second;
        return result;
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Moment other = (Moment)obj;
        return toInt() == other.toInt();
    }
    public int compareTo(Object obj) {
        if (!(obj instanceof Moment)) {
            throw new IllegalArgumentException( "compare to what?" );
        }
        return toInt() - ((Moment)obj).toInt();
    }
}