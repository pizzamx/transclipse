/**
 * @author pizza(pizzamx@gmail.com)
 * ===============
 * In God We Trust
 * ===============
 * 2007-3-3 ÏÂÎç04:43:49
 *
 */
package subtitle.utils;

import java.text.NumberFormat;

import subtitle.model.Moment;


public class TimelineHelper {
    private static NumberFormat formatter = NumberFormat.getInstance();

    public static String[] split(String text) {
        String[] parts = new String[2];
        int pos = text.indexOf( " --> " );
        if (pos != -1) {
            parts[0] = text.substring( 0, pos );
            parts[1] = text.substring( pos + 5 );
        }
        return parts;
    }

    public static String merge(String part1, String part2) {
        StringBuffer newPart = new StringBuffer();
        return newPart.append( part1 ).append( " --> " ).append( part2 ).toString();
    }
    public static int getDistance(Moment latter, Moment former) {
        int h2 = latter.getHour(), m2 = latter.getMin(), s2 = latter.getSecond(), ms2 = latter.getMillisecond();
        int h1 = former.getHour(), m1 = former.getMin(), s1 = former.getSecond(), ms1 = former.getMillisecond();
        return h1 == h2 ? m1 == m2 ? s2 * 1000 + ms2 - s1 * 1000 - ms1 : (m2 * 60 + s2) * 1000 + ms2 - (m1 * 60 + s1) * 1000 - ms1 : ((h2 * 60 + m2) * 60 + s2) * 1000
                + ms2 - ((h1 * 60 + m1) * 60 + s1) * 1000 - ms1;
    }

    public static String generateShortTimeline(Moment from, Moment to) {
        return from.getHour() + ":" + from.getMin() + ":" + from.getSecond() + "-" + to.getHour() + ":" + to.getMin() + ":" + to.getSecond();
    }
    public static String generateTimeline(Moment from, Moment to) {
        StringBuffer sb = new StringBuffer();
        formatter.setMinimumIntegerDigits( 2 );
        sb.append( formatter.format( from.getHour() ) ).append( ':' ).append( formatter.format( from.getMin() ) ).append( ':' ).append(
                formatter.format( from.getSecond() ) );
        formatter.setMinimumIntegerDigits( 3 );
        sb.append( ',' ).append( formatter.format( from.getMillisecond() ) ).append( " --> " );
        formatter.setMinimumIntegerDigits( 2 );
        sb.append( formatter.format( to.getHour() ) ).append( ':' ).append( formatter.format( to.getMin() ) ).append( ':' ).append( formatter.format( to.getSecond() ) );
        formatter.setMinimumIntegerDigits( 3 );
        sb.append( ',' ).append( formatter.format( to.getMillisecond() ) );
        return sb.toString();
    }
    public static void main(String[] args) {
        System.out.println( generateTimeline( new Moment( "00:43:59,234" ), new Moment( "00:44:02,701" ) ) );
    }
}
