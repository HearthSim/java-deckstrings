package net.hearthsim.java.deckstrings;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Deckstrings {
    public static int FT_STANDARD = 1;
    public static int FT_WILD = 2;

    private static int VERSION = 1;

    public static class Result {
        public List<Integer> heroes;
        public Integer format;
        public List<Card> cards;
    }

    public static class Card {
        public int dbfId;
        public int count; // number of times this card appears in the deck

        public Card(int dbfId_, int count_) {
            this.dbfId = dbfId_;
            this.count = count_;
        }
    }

    public static class ParseException extends Exception {
        ParseException(String message) {
            super(message);
        }
    }

    /**
     *
     * @param data the base64 decoded data. This method intentionnaly does not
     *             decode Base64 as implementation differ greatly between android and
     *             other JVM. See https://github.com/auth0/java-jwt/issues/131 for more
     *             details
     * @return
     * @throws Exception
     */
    public static Result decode(byte[] data) throws Exception {
        Result result = new Result();

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        byteBuffer.get(); // reserverd
        int version = byteBuffer.get();
        if (version != VERSION) {
            throw new ParseException("bad version: " + version);
        }

        result.format = VarInt.getVarInt(byteBuffer);
        if (result.format != FT_STANDARD && result.format != FT_WILD) {
            throw new ParseException("bad format: " + result.format);
        }


        int heroCount = VarInt.getVarInt(byteBuffer);
        result.heroes = new ArrayList<>();
        for (int i = 0; i < heroCount; i++) {
            result.heroes.add(VarInt.getVarInt(byteBuffer));
        }

        result.cards = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            int c = VarInt.getVarInt(byteBuffer);
            for (int j = 0; j < c; j++) {
                int dbfId = VarInt.getVarInt(byteBuffer);
                int count;
                if (i == 3) {
                    count = VarInt.getVarInt(byteBuffer);
                } else {
                    count = i;
                }
                result.cards.add(new Card(dbfId, count));
            }
        }

        Collections.sort(result.cards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return o2.dbfId - o1.dbfId;
            }
        });

        return result;
    }
}
