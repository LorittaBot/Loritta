package com.mrpowergamerbr.loritta.utils.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AudioTrackWrapper {
	private AudioTrack track;
	private boolean isAutoPlay;
}
