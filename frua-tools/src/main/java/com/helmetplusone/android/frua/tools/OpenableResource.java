package com.helmetplusone.android.frua.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
interface OpenableResource {
    InputStream inputSteam() throws IOException;

    String name();
}
