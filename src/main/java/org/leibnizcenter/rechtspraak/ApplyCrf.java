package org.leibnizcenter.rechtspraak;

import cc.mallet.fst.CRF;
import cc.mallet.fst.SimpleTagger;
import cc.mallet.types.Instance;
import cc.mallet.types.Sequence;
import org.leibnizcenter.rechtspraak.markup.Const;
import org.leibnizcenter.rechtspraak.markup.RechtspraakCorpus;
import org.leibnizcenter.rechtspraak.markup.RechtspraakTokenList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by maarten on 13-3-16.
 */
public class ApplyCrf {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CRF crf = loadCrf(new File(Const.RECHTSPRAAK_MARKUP_TAGGER_CRF));
        List<File> files = RechtspraakCorpus.listXmlFiles().subList(0, 3);
        for (RechtspraakTokenList doc : new RechtspraakTokenList.FileIterable(files)) {
            Instance instance = TrainWithMallet.getInstance(doc, true);
            Sequence data = (Sequence) instance.getData();
            Sequence[] labels = SimpleTagger.apply(crf, (Sequence) TrainWithMallet.pipe.pipe(instance).getData(), 3);
            for (Sequence s : labels) {
                for (int i = 0; i < s.size(); i++) {
                    Object label = s.get(i);
                    //RechtspraakToken taggedToken = doc.get(i);
                    System.out.println(label + ": " + data.get(i));
//                    if (!taggedToken.getTag().toString().equals(label)) {
//                        System.out.println("---------------------------------");
//                        System.out.println(label + ": " + ((Token) data.get(i)));
//                        System.out.println(taggedToken.getTag() + ": " + ((Token) data.get(i)));
//                        System.out.println("---------------------------------");
//                    }
                }
                System.out.println("------------------------");
            }
            System.out.println("------------------------");
        }

        //TODO write stuff to xml
    }

    private static CRF loadCrf(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (CRF) ois.readObject();
    }
}