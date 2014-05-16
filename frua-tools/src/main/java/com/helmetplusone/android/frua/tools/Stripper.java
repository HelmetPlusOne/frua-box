package com.helmetplusone.android.frua.tools;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
abstract class Stripper {
    String strip(String name) {
        return name;
    }

    static class Abandonia extends Stripper {
        @Override
        public String strip(String name) {
            return name.substring(5);
        }
    }

    static class DosGraveyard extends Stripper {}

    static class GamesWin extends Stripper {
        @Override
        String strip(String name) {
            return name.substring(5);
        }
    }

    static class OldSchoolApps extends Stripper {
        @Override
        String strip(String name) {
            return name.substring(5);
        }
    }

    static class XtcAbandonware extends Stripper {
        @Override
        String strip(String name) {
            return name.substring(7);
        }
    }
}
