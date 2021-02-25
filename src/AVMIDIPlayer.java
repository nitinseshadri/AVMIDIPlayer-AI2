package com.nitinseshadri.aix.AVMIDIPlayer;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.errors.*;
import com.google.appinventor.components.runtime.util.*;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import android.media.MediaPlayer;
import android.net.Uri;

@DesignerComponent(version = AVMIDIPlayer.VERSION,
    description = "by nitinseshadri",
    category = ComponentCategory.EXTENSION,
    nonVisible = true)

@SimpleObject(external = true)

@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE")

public class AVMIDIPlayer extends AndroidNonvisibleComponent
implements Component {
    public static final int VERSION = 1;
    private ComponentContainer container;
    private Context context;
    private final Activity activity;
    private static final String LOG_TAG = "AVMIDIPlayer";
    public AVMIDIPlayer(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        activity = (Activity) container.$context();
        context = (Context) container.$context();
        Log.d(LOG_TAG, "AVMIDIPlayer Created");
    }

    // Variables //

    private MediaPlayer mediaPlayer;
    private Boolean ready = false; // Are we ready for launch?
    private Boolean debug = true; // Set to true to enable debug toasts.

    // Helper Functions //

    private String AbsoluteFileName(String filename) {
      // This is a very simplified version of the AbsoluteFileName function found in
      // the File component from the App Inventor sources. Future versions of this extension
      // should implement that.
      if (filename.startsWith("//")) { // Assets directory
        // HACK: Copy file to private data directory, then return that path
        InputStream stream = null;
        OutputStream output = null;
        try {
          stream = activity.getAssets().open(filename.substring(2));
          output = new BufferedOutputStream(new FileOutputStream(activity.getFilesDir().getPath() + "/" + filename.substring(2), false));
          byte data[] = new byte[1024];
          int count;
          while((count = stream.read(data)) != -1) {
            output.write(data, 0, count);
          }
          output.flush();
          output.close();
          stream.close();
        } catch (Throwable e) {
          HandleException(e);
        } finally {
          stream = null;
          output = null;
          return activity.getFilesDir().getPath() + "/" + filename.substring(2);
        }
      } else if (filename.startsWith("/")) { // External storage
        return Environment.getExternalStorageDirectory().getPath() + filename;
      } else { // Private data directory
        return activity.getFilesDir().getPath() + "/" + filename;
      }
    }

    private void HandleException(Throwable e) {
      ready = false;
      if (e instanceof PermissionException) {
        form.dispatchPermissionDeniedEvent(this, "Init", (PermissionException) e);
      } else if (e instanceof FileNotFoundException) {
        form.dispatchErrorOccurredEvent(this, "Init",
            ErrorMessages.ERROR_CANNOT_FIND_FILE, e.getMessage());
      } else if (e instanceof IOException) {
        form.dispatchErrorOccurredEvent(this, "Init",
            ErrorMessages.ERROR_CANNOT_READ_FILE, e.getMessage());
      } else {
        form.dispatchErrorOccurredEvent(this, "Init",
            ErrorMessages.ERROR_DEFAULT, e.getMessage());
      }
      ShowDebugToast(e.getMessage());
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }

    private void ShowDebugToast(String message) {
      if (debug) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
      }
    }

    // Methods //

    @SimpleFunction(description = "Initializes a newly allocated MIDI player with the contents of the file specified by the URL, using the specified sound bank.")
    public void Init(String contentsOf, String soundBankURL) {
      try {
        mediaPlayer = MediaPlayer.create(context, Uri.parse(AbsoluteFileName(contentsOf)));
        ready = true;
        ShowDebugToast("Init successful");
      } catch (Throwable e) {
        HandleException(e);
      }
    }

    @SimpleFunction(description = "Prepares to play the sequence by prerolling all events.")
    public void PrepareToPlay() {
      if (ready) {
        try {
          mediaPlayer.prepare();
        } catch (Throwable e) {
          HandleException(e);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "PrepareToPlay",
            ErrorMessages.ERROR_DEFAULT);
      }
    }

    @SimpleFunction(description = "Plays the sequence.")
    public void Play() {
      if (ready) {
        mediaPlayer.start();
      } else {
        form.dispatchErrorOccurredEvent(this, "Play",
            ErrorMessages.ERROR_DEFAULT);
      }
    }

    @SimpleFunction(description = "Stops playing the sequence.")
    public void Stop() {
      if (ready) {
        mediaPlayer.pause();
      } else {
        form.dispatchErrorOccurredEvent(this, "Stop",
            ErrorMessages.ERROR_DEFAULT);
      }
    }

    // Events //

    @SimpleEvent(description = "A block to be called when a MIDI playback request is completed.")
    public void Completed() {
      EventDispatcher.dispatchEvent(this, "Completed");
    }

    // Properties //

    @SimpleProperty(description = "A Boolean value that indicates whether the sequence is playing.")
    public boolean IsPlaying() {
      if (ready) {
        return mediaPlayer.isPlaying();
      } else {
        form.dispatchErrorOccurredEvent(this, "IsPlaying",
            ErrorMessages.ERROR_DEFAULT);
        return false;
      }
    }

    @SimpleProperty(description = "The length of the currently loaded file, in seconds.")
    public int Duration() {
      if (ready) {
        return mediaPlayer.getDuration() / 1000000;
      } else {
        form.dispatchErrorOccurredEvent(this, "Duration",
            ErrorMessages.ERROR_DEFAULT);
        return 0;
      }
    }

    @SimpleProperty(description = "The current playback position, in seconds.")
    public int CurrentPosition() {
      // Getter method
      if (ready) {
        return mediaPlayer.getCurrentPosition() / 1000000;
      } else {
        form.dispatchErrorOccurredEvent(this, "CurrentPosition",
            ErrorMessages.ERROR_DEFAULT);
        return 0;
      }
    }
    
    @SimpleProperty(description = "The current playback position, in seconds.")
    public void CurrentPosition(int currentPosition) {
      // Setter method
      if (ready) {
        mediaPlayer.seekTo(currentPosition * 1000000);
      } else {
        form.dispatchErrorOccurredEvent(this, "CurrentPosition",
            ErrorMessages.ERROR_DEFAULT);
      }
    }

    @SimpleProperty(description = "The playback rate of the player.")
    public float Rate() {
      // Getter method
      if (ready) {
        //TODO
        return 1.0f;
      } else {
        form.dispatchErrorOccurredEvent(this, "Rate",
            ErrorMessages.ERROR_DEFAULT);
        return 0;
      }
    }
    
    @SimpleProperty(description = "The playback rate of the player.")
    public void Rate(float rate) {
      // Setter method
      if (ready) {
        //TODO
      } else {
        form.dispatchErrorOccurredEvent(this, "Rate",
            ErrorMessages.ERROR_DEFAULT);
      }
    }

}