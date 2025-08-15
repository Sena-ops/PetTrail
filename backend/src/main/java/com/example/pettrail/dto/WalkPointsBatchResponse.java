package com.example.pettrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response summary for walk points batch processing")
public class WalkPointsBatchResponse {

    @Schema(description = "Total number of points received in the request", example = "100")
    private int received;

    @Schema(description = "Number of points successfully accepted and stored", example = "95")
    private int accepted;

    @Schema(description = "Number of points discarded due to validation or outlier detection", example = "5")
    private int discarded;

    // Constructors
    public WalkPointsBatchResponse() {}

    public WalkPointsBatchResponse(int received, int accepted, int discarded) {
        this.received = received;
        this.accepted = accepted;
        this.discarded = discarded;
    }

    // Getters and Setters
    public int getReceived() {
        return received;
    }

    public void setReceived(int received) {
        this.received = received;
    }

    public int getAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }

    public int getDiscarded() {
        return discarded;
    }

    public void setDiscarded(int discarded) {
        this.discarded = discarded;
    }
}
