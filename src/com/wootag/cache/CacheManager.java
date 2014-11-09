package com.wootag.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public final class CacheManager {

    public static String cacheDir;

    private final Context context;
    private static CacheManager singleton;
    private static File cacheFile;
    private static String cacheDirPath = "/Wootag/Cache/";
    private static final Logger LOG = LoggerManager.getLogger();

    private CacheManager(final Context context) {

        this.context = context;
        cacheDir = FileCache(this.context).getAbsolutePath().toString() + cacheDirPath;
        final File file = new File(cacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        LOG.d("Initializing new instance");
    }

    public static File FileCache(final Context context) {

        cacheFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        return cacheFile;
    }

    public static CacheManager getInstance(final Context context) {

        cacheDir = FileCache(context).getAbsolutePath().toString() + cacheDirPath;
        final File file = new File(cacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (singleton == null) {
            singleton = new CacheManager(context);
        }
        return singleton;
    }

    /**
     * Deletes a file in the cache directory.
     *
     * @param fileName The file to delete.
     */
    public void deleteFile(final String fileName) {

        LOG.d("Deleting the file " + cacheDir + fileName);
        final File toDelete = new File(cacheDir, fileName);
        toDelete.delete();
    }

    /**
     * Reads an array of bytes from an existing file in the cache directory and returns it.
     *
     * @param fileName The file name of an existing file in the cache directory to be read.
     * @return The byte array that was read
     * @throws CacheTransactionException Throws the exception if reading failed. Will not throw an exception in the
     *             result of a successful read.
     */
    public byte[] readBinaryFile(final String fileName) throws CacheTransactionException {

        RandomAccessFile randomAccessFile = null;
        try {
            final File file = new File(cacheDir, fileName);
            randomAccessFile = new RandomAccessFile(file, "r");
            final byte[] byteArray = new byte[(int) randomAccessFile.length()];
            randomAccessFile.read(byteArray);
            return byteArray;
        } catch (final Exception e) {
            LOG.d("Unsuccessful read from " + cacheDir + fileName);
            throw new CacheTransactionException(Constant.readExceptionAlert);
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

    }

    /**
     * Reads a bitmap from the specified file and returns the bitmap.
     *
     * @param fileName The File name that will be read from.
     * @return Returns the bitmap in the case of a successful read.
     * @throws CacheTransactionException CacheTransactionException Throws the exception if reading failed. Will not
     *             throw an exception in the result of a successful read.
     */
    public Bitmap readBitmap(final String fileName) throws CacheTransactionException {

        final File file = new File(cacheDir, fileName);
        final Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
        if (bitmap != null) {
            return bitmap;
        }
        throw new CacheTransactionException(Constant.readExceptionAlert);
    }

    /**
     * Reads a JSONObject from a string file. Initially runs readString(), so there may be logs saying there was a
     * successful read, but the log will be followed up by another log stating that it was unable to create a JSONObject
     * from the read string.
     *
     * @param fileName The file name that will be read from.
     * @return The JSONObject the file was storing, in the result of a successful read.
     * @throws CacheTransactionException Throws the exception if reading failed, or the creation of the JSONObject
     *             fails.
     */
    public JSONObject readJSONObject(final String fileName) throws CacheTransactionException {

        final String JSONString = this.readString(fileName); // Will throw exception here if string read fails...
        try {
            return new JSONObject(JSONString);
        } catch (final JSONException e) {
            LOG.d("Successfully read the file " + cacheDir + fileName
                    + ", but was unable to create a JSONObject from the String.");
            throw new CacheTransactionException(Constant.readExceptionAlert);
        }
    }

    // =======================================
    // ========== JSON Read/Write ============
    // =======================================

    /**
     * Reads an encrypted JSONObject from a string file. Initially runs readString(), so there may be logs saying there
     * was a successful read, but the log will be followed up by another log stating that it was unable to create a
     * JSONObject from the read string.
     *
     * @param fileName The file name that will be read from.
     * @param key The encryption/decryption key that was used to write to this file.
     * @return The JSONObject the file was storing, in the result of a successful read.
     * @throws CacheTransactionException Throws the exception if reading failed, or the creation of the JSONObject
     *             fails.
     */
    // public JSONObject readJSONObjectEncrypted(final String fileName, final String key) throws
    // CacheTransactionException {
    //
    // final String JSONString = this.readStringEncrypted(fileName, key); // Will throw exception here if string read
    // // fails...
    // try {
    // final JSONObject obj = new JSONObject(JSONString);
    // return obj;
    // } catch (final JSONException e) {
    // LOG.d( "Successfully read the file " + mCacheDir + fileName
    // + ", but was unable to create a JSONObject from the String.");
    // throw new CacheTransactionException(Constant.readExceptionAlert);
    // }
    // }

    /**
     * Reads a string from an existing file in the cache directory and returns it.
     *
     * @param fileName The file name of an existing file in the cache directory to be read.
     * @return Returns whatever is read. Null if read fails.
     * @throws CacheTransactionException Throws the exception if reading failed. Will not throw an exception in the
     *             result of a successful read.
     */
    public String readString(final String fileName) throws CacheTransactionException {

        String readString = "";
        final File file = new File(cacheDir, fileName);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));

            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                readString += currentLine;
            }
            LOG.d("Reading from " + cacheDir + fileName);
            return readString;
        } catch (final IOException e) {
            LOG.d("Unsuccessful read from " + cacheDir + fileName);
            throw new CacheTransactionException(Constant.readExceptionAlert);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
    }

    /**
     * Reads a string from an existing file in the cache directory, decrypts it, then returns it.
     *
     * @param fileName The file name of an existing file in the cache directory to be read.
     * @param key The encryption/decryption key that was used to write to this file.
     * @return Returns the decrypted version of what is read.
     * @throws CacheTransactionException Throws the exception if reading failed. Will not throw an exception in the
     *             result of a successful read.
     */
    // public String readStringEncrypted(final String fileName, final String key) throws CacheTransactionException {
    //
    // final BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    // textEncryptor.setPassword(key);
    // final String encrypted = this.readString(fileName); // Will throw here if nothing is read
    // final String decrypted = textEncryptor.decrypt(encrypted);
    // LOG.d( "Decrypting for a read from " + mCacheDir + fileName);
    // return decrypted;
    // }

    /**
     * Writes a Bitmap to the given file name. The file will be placed in the current application's cache directory.
     *
     * @param bitmap The Bitmap to be written to cache.
     * @param format The format that the Bitmap will be written to cache. (Either CompressFormat.PNG,
     *            CompressFormat.JPEG, or CompressFormat.WEBP)
     * @param quality The quality that the Bitmap will be written at. 0 is the lowest quality, 100 is the highest
     *            quality. If you are writing as .PNG format, this parameter will not matter as PNG is lossless.
     * @param fileName The File name that will be written to.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    public void write(final Bitmap bitmap, final CompressFormat format, final int quality, final String fileName)
            throws CacheTransactionException {

        final File file = new File(cacheDir, fileName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(format, quality, out);
        } catch (final Exception e) {
            LOG.d("Unsuccessful write to " + cacheDir + fileName);
            throw new CacheTransactionException(Constant.writeExceptionAlert);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
    }

    // =======================================
    // ========= Bitmap Read/Write ===========
    // =======================================

    /**
     * Writes an array of bytes to the given file name. The file will be placed in the current application's cache
     * directory.
     *
     * @param toWrite The byte array to write to a file.
     * @param fileName The File name that will be written to.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    public void write(final byte[] toWrite, final String fileName) throws CacheTransactionException {

        final File file = new File(cacheDir, fileName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            out.write(toWrite);
        } catch (final Exception e) {
            LOG.d("Unsuccessful write to " + cacheDir + fileName);
            throw new CacheTransactionException(Constant.writeExceptionAlert);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
    }

    /**
     * Writes a JSONObject to cache as a readable string to cache. If JSONObject stores sensitive data use
     * writeEncrypted for the JSONObject.
     *
     * @param obj The JSONObject to write.
     * @param fileName The File name that will be written to.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    public void write(final JSONObject obj, final String fileName) throws CacheTransactionException {

        this.write(obj.toString(), fileName);
    }

    // =======================================
    // ========== Binary Read/Write ==========
    // =======================================

    /**
     * Writes a string to the given file name. The file will be placed in the current application's cache directory.
     *
     * @param toWrite The String to write to a file.
     * @param fileName The File name that will be written to.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    public void write(final String toWrite, final String fileName) throws CacheTransactionException {

        final File file = new File(cacheDir, fileName);

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file, false), 1024);
            // out.write(toWrite);
            LOG.d("Writing to string length " + toWrite.length());
            out.write(toWrite, 0, toWrite.length());
            LOG.d("Writing to " + cacheDir + fileName);
        } catch (final IOException e) {
            LOG.d("Unsuccessful write to " + cacheDir + fileName);
            throw new CacheTransactionException(Constant.writeExceptionAlert);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (final IOException exception) {
                    LOG.e(exception);

                }
            }
        }
    }

    /**
     * Writes the JSONObject as an encrypted string to cache.
     *
     * @param obj The JSONObject to write.
     * @param fileName The File name that will be written to.
     * @param key The encryption/decryption key that will be used to read from this file.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    // public void writeEncrypted(final JSONObject obj, final String fileName, final String key)
    // throws CacheTransactionException {
    //
    // this.writeEncrypted(obj.toString(), fileName, key);
    // }

    // ===========================================
    // ========== FileSystem Management ==========
    // ===========================================

    /**
     * Encrypts, and then writes a string to the given file name. The file will be placed in the current application's
     * cache directory.
     *
     * @param toWrite The String to write to a file.
     * @param fileName The File name that will be written to.
     * @param key The encryption/decryption key that will be used to write + read from this file.
     * @throws CacheTransactionException Throws the exception if writing failed. Will not throw an exception in the
     *             result of a successful write.
     */
    // public void writeEncrypted(final String toWrite, final String fileName, final String key)
    // throws CacheTransactionException {
    //
    // LOG.d( "Encrypting for a write to " + mCacheDir + fileName);
    // final BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    // textEncryptor.setPassword(key);
    // final String encrypted = textEncryptor.encrypt(toWrite);
    // this.write(encrypted, fileName);
    // }

}
