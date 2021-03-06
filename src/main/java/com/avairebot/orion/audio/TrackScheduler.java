package com.avairebot.orion.audio;

import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    private final GuildMusicManager manager;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrackContainer> queue;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(GuildMusicManager manager, AudioPlayer player) {
        this.manager = manager;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track, User requester) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        AudioTrackContainer container = new AudioTrackContainer(track, requester);

        if (!player.startTrack(track, true)) {
            queue.offer(container);
            return;
        }

        if (manager.getLastActiveMessage() != null) {
            sendNowPlaying(container);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrackContainer container = queue.poll();
        player.startTrack(container.getAudioTrack(), false);

        if (manager.getLastActiveMessage() != null) {
            sendNowPlaying(container);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.equals(AudioTrackEndReason.FINISHED) && queue.isEmpty()) {
            if (manager.getLastActiveMessage() != null) {
                MessageFactory.makeSuccess(manager.getLastActiveMessage(), "Queue has ended, leaving voice.").queue();
                manager.getLastActiveMessage().getGuild().getAudioManager().closeAudioConnection();
            }
            return;
        }

        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public BlockingQueue<AudioTrackContainer> getQueue() {
        return queue;
    }

    protected void sendNowPlaying(AudioTrackContainer container) {
        MessageFactory.makeSuccess(manager.getLastActiveMessage(), "Now playing: [%s](%s)\n`%s` - Requested by <@%s>",
                container.getAudioTrack().getInfo().title,
                container.getAudioTrack().getInfo().uri,
                container.getFormattedDuration(),
                container.getRequester().getId()
        ).queue();
    }
}