/**
 * Created on 2008-1-11
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import subtitle.model.SubtitleUnit;

public class SubtitleReader {
    public final static String NEW_LINE = System.getProperty( "line.separator" );

    private File file;

    public SubtitleReader(File file) {
        this.file = file;
    }
    public List read() throws IOException {
        final List list = new LinkedList();
        final List tempStorage = new ArrayList();
        BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), Charset.forName( "GBK" ) ) );
        String line;
        while ((line = reader.readLine()) != null) {
            tempStorage.add( line );
        }
        reader.close();

        final int size = tempStorage.size();
        String t;
        SubtitleUnit unit;
        int index = 0;
        while (index < size) {
            if ("".equals( ((String)tempStorage.get( index++ )).trim() )) {
                /*
                 * if a subtitle-line is followed by more one blank lines, we
                 * should skip it but this does not conform to the standard, see
                 * http://www.matroska.org/technical/specs/subtitles/srt.html
                 */
                continue;
            }
            int field = 1;

            line = (String)tempStorage.get( index ); // subtitle-line
            // index,
            // just skip
            // it
            unit = new SubtitleUnit();

            try {
                line = (String)tempStorage.get( index++ );
                unit.setTimeline( line );
            } catch (Exception e) {
                System.out.println( "Broke on line:" + index );
                e.printStackTrace();
            }

            line = (String)tempStorage.get( index );
            do {
                t = unit.getOriginal();
                unit.setOriginal( t == null ? line : new StringBuffer( t ).append( NEW_LINE ).append( line ).toString() );
                if (index++ < size - 1) {
                    line = (String)tempStorage.get( index );
                } else {
                    break;
                }
            } while (!"".equals( line ));
            list.add( unit );
            index++;
        }
        return list;
    }
    public static void main(String[] args) throws IOException {
        List ref, hard;
        SubtitleReader reader = new SubtitleReader( new File( "e:\\ripz\\The.Hunting.Party.LIMITED.DVDRip.XviD-SAPHiRE\\sph-huntingp.ref.srt" ) );
        ref = reader.read();
        reader.setFile( new File( "e:\\ripz\\The.Hunting.Party.LIMITED.DVDRip.XviD-SAPHiRE\\hard.srt" ) );
        hard = reader.read();
        ref.addAll( hard );
        Collections.sort( ref, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                SubtitleUnit a = (SubtitleUnit)arg0, b = (SubtitleUnit)arg1;
                return a.getStart().toInt() - b.getStart().toInt();
            }
        } );

        File file = new File( "e:\\ripz\\The.Hunting.Party.LIMITED.DVDRip.XviD-SAPHiRE\\new.srt" );
        BufferedWriter refWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ) ) );
        SubtitleUnit unit;
        String o, t;
        for (int i = 0; i < ref.size(); i++) {
            unit = (SubtitleUnit)ref.get( i );
            refWriter.write( i + 1 + SubtitleReader.NEW_LINE );
            refWriter.write( unit.getTimeline() + SubtitleReader.NEW_LINE );
            o = unit.getOriginal();
            // if (t == null || "".equals( t.trim() )) {
            // t = unit.getOriginal();
            // }
            refWriter.write( o + SubtitleReader.NEW_LINE + SubtitleReader.NEW_LINE );
        }
        refWriter.close();

    }
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
}
