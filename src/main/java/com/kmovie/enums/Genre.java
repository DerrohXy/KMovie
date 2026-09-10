package com.kmovie.enums;

/**
 * Titles can carry multiple genres. They are persisted on the Title entity
 * as a single comma-concatenated string column (see Title#genre) and
 * exposed to API clients as a List<Genre>.
 */
public enum Genre {
    ACTION,
    ADVENTURE,
    ANIMATION,
    COMEDY,
    CRIME,
    DOCUMENTARY,
    DRAMA,
    FAMILY,
    FANTASY,
    HISTORY,
    HORROR,
    MUSICAL,
    MYSTERY,
    ROMANCE,
    SCI_FI,
    SPORT,
    THRILLER,
    WAR,
    WESTERN
}
