/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.model;

/**
 * Provides a strongly-typed representation of a Location as defined by the Graph API. Note that this interface is
 * intended to be used with GraphObject.Factory and not implemented directly.
 */
public interface GraphLocation extends GraphObject {

    /**
     * Gets the city component of the location.
     *
     * @return the city component of the location
     */
    String getCity();

    /**
     * Returns the country component of the location.
     *
     * @return the country component of the location
     */
    String getCountry();

    /**
     * Returns the latitude component of the location.
     *
     * @return the latitude component of the location
     */
    double getLatitude();

    /**
     * Returns the longitude component of the location.
     *
     * @return the longitude component of the location
     */
    double getLongitude();

    /**
     * Returns the state component of the location.
     *
     * @return the state component of the location
     */
    String getState();

    /**
     * Returns the street component of the location.
     *
     * @return the street component of the location, or null
     */
    String getStreet();

    /**
     * Returns the postal code component of the location.
     *
     * @return the postal code component of the location
     */
    String getZip();

    /**
     * Sets the city component of the location.
     *
     * @param city the city component of the location
     */
    void setCity(String city);

    /**
     * Sets the country component of the location
     *
     * @param country the country component of the location
     */
    void setCountry(String country);

    /**
     * Sets the latitude component of the location.
     *
     * @param latitude the latitude component of the location
     */
    void setLatitude(double latitude);

    /**
     * Sets the longitude component of the location.
     *
     * @param longitude the longitude component of the location
     */
    void setLongitude(double longitude);

    /**
     * Sets the state component of the location.
     *
     * @param state the state component of the location
     */
    void setState(String state);

    /**
     * Sets the street component of the location.
     *
     * @param street the street component of the location, or null
     */
    void setStreet(String street);

    /**
     * Sets the postal code component of the location.
     *
     * @param zip the postal code component of the location
     */
    void setZip(String zip);
}
