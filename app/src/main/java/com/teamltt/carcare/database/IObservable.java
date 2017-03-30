/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.database;

import android.os.Bundle;

/**
 * In the Observer pattern, An observable object (subject) maintains a list of its observers.
 * The subject notifies the observers when the state changes.
 * See more at https://en.wikipedia.org/wiki/Observer_pattern
 */
public interface IObservable {

    /**
     * A method for registering an observer to a subject
     *
     * @param observer the observer to register
     */
    void addObserver(IObserver observer);

    /**
     * @return the number of registered observers
     */
    int countObservers();

    /**
     * A method for deregistering an observer to a subject
     * @param observer the observer to deregister
     */
    void deleteObserver(IObserver observer);

    /**
     * A method to deregister all observers of a subject
     */
    void deleteObservers();

    /**
     * A method to detect if the subject has state changes
     * @return true if the state has changed, false otherwise.
     */
    boolean hasChanged();

    /**
     * A method to notify the observers of the new state changes.
     * @param args the state changes
     */
    void notifyObservers(Bundle args);

    /**
     * A method to notify the observers of state changes.
     * Passes a default state to notifyObservers(Bundle args)
     */
    void notifyObservers();
}
