package com.helmetplusone.android.frua.tools;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
class RawOpenableResource implements OpenableResource {
    private final Context context;
    private final int id;

    public RawOpenableResource(Context context, int id) {
        this.context = context;
        this.id = id;
    }

    @Override
    public InputStream inputSteam() throws IOException {
        return context.getResources().openRawResource(id);
    }

    @Override
    public String name() {
        return context.getResources().getResourceEntryName(id);
    }
}
