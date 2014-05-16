package com.helmetplusone.android.frua;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.helmetplusone.android.frua.R;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static java.util.Locale.US;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
public class FsAdapter extends ArrayAdapter<String> {
    static final String TO_PARENT_DIR = "..";
    private static final Comparator<File> CI_COMP = new Comp();

    private final FileFilter filter;
    private File[] files;
    private boolean hasParent = false;


    public FsAdapter(Context context, FileFilter filter, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        if(null == filter) throw new IllegalArgumentException("Provided filter is null");
        this.filter = filter;
    }

    void flip(File dir) throws IOException {
        if(null == dir) throw new IllegalArgumentException("Provided dir is null");
        if(!dir.exists() && dir.isDirectory() && dir.canRead()) throw new IOException(
                "Cannot access directory: [" + dir.getAbsolutePath() + "]");
        this.files = dir.listFiles(filter);
        Arrays.sort(files, CI_COMP);
        String[] strings = new String[files.length];
        for (int i = 0; i < files.length; i++) strings[i] = files[i].getName();
        hasParent = null != dir.getParent();
        clear();
        if(hasParent) add(TO_PARENT_DIR);
        for(String st : strings) add(st);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        ImageView icon = (ImageView) row.findViewById(R.id.dir_icon);
        final boolean isDir;
        if (position > 0 || !hasParent) {
            File fi = files[position - (hasParent ? 1 : 0)];
            isDir = fi.isDirectory();
        } else isDir = false;
        int id = isDir ? R.drawable.ic_menu_archive : R.drawable.ic_menu_spacer;
        icon.setImageResource(id);
        return row;
    }

    private static class Comp implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            if(o1.isDirectory() && !o2.isDirectory()) return -1;
            if(o2.isDirectory() && !o1.isDirectory()) return 1;
            String name1 = o1.getName().toLowerCase(US);
            String name2 = o2.getName().toLowerCase(US);
            return name1.compareTo(name2);
        }
    }
}
