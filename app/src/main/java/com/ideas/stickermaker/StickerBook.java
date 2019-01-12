package com.ideas.stickermaker;


import android.content.Context;
import android.util.Log;

import com.ideas.stickermaker.WhatsAppBasedCode.StickerPackModal;

import java.io.File;
import java.util.ArrayList;

public class StickerBook {

    static Context myContext;
    public static ArrayList<StickerPackModal> allStickerPacks = checkIfPacksAreNull();

    public static void init(Context context) {
        myContext = context;
        ArrayList<StickerPackModal> lsp = DataArchiver.readStickerPackJSON(context);
        if (lsp != null && lsp.size() != 0) {
            allStickerPacks = lsp;
        }
    }

    private static ArrayList<StickerPackModal> checkIfPacksAreNull() {
        if (allStickerPacks == null) {
            Log.w("IS PACKS NULL?", "YES");
            return new ArrayList<>();
        }
        Log.w("IS PACKS NULL?", "NO");
        return allStickerPacks;
    }


    public static void addStickerPackExisting(StickerPackModal sp) {
        allStickerPacks.add(sp);
    }


    public static ArrayList<StickerPackModal> getAllStickerPacks() {
        return allStickerPacks;
    }

    public static StickerPackModal getStickerPackByName(String stickerPackName) {
        for (StickerPackModal sp : allStickerPacks) {
            if (sp.getName().equals(stickerPackName)) {
                return sp;
            }
        }
        return null;
    }

    public static StickerPackModal getStickerPackById(String stickerPackId) {
        if (allStickerPacks.isEmpty()) {
            init(myContext);
        }
        Log.w("THIS IS THE ALL STICKER", allStickerPacks.toString());
        for (StickerPackModal sp : allStickerPacks) {
            if (sp.getIdentifier().equals(stickerPackId)) {
                return sp;
            }
        }
        return null;
    }

    public static StickerPackModal getStickerPackByIdWithContext(String stickerPackId, Context context) {
        if (allStickerPacks.isEmpty()) {
            init(context);
        }
        Log.w("THIS IS THE ALL STICKER", allStickerPacks.toString());
        for (StickerPackModal sp : allStickerPacks) {
            if (sp.getIdentifier().equals(stickerPackId)) {
                return sp;
            }
        }
        return null;
    }

    public static void deleteStickerPackById(String stickerPackId) {
        StickerPackModal myStickerPack = getStickerPackById(stickerPackId);
        new File(myStickerPack.getTrayImageUri().getPath()).getParentFile().delete();
        allStickerPacks.remove(myStickerPack);
    }

    public static StickerPackModal getStickerPackByIndex(int index) {
        return allStickerPacks.get(index);
    }
}
