package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;

/**
 * Valdilis(interface) kuris turėtų veikit kaip filtras prisijugiantiems vartotojams.
 * Čia galima apibrėžti iš kokių ip klientų nepriimti.
 * Šiuo metu klientai arba įleidžaimi arba ne.
 */
public interface PrisijungimuFiltras {

    PrisijungimuFiltras ATMESK_VISUS = new PrisijungimuFiltras() {
        public boolean priimkPrisijungima(InetSocketAddress adresas) {
            return false;
        }
    };

    PrisijungimuFiltras LEISK_VISUS = new PrisijungimuFiltras() {
        public boolean priimkPrisijungima(InetSocketAddress adresas) {
            return true;
        }
    };

    public boolean priimkPrisijungima(InetSocketAddress adresas);
}
