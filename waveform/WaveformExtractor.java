// Decompiled by DJ v3.6.6.79 Copyright 2004 Atanas Neshkov  Date: 2017-08-02 ¿ÀÀü 9:20:47
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   WaveformExtractor.java

package waveform;

import javax.sound.sampled.AudioInputStream;

public interface WaveformExtractor
{

    public abstract int[] extract(AudioInputStream audioinputstream);
}