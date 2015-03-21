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
 * Provides a strongly-typed representation of a User as defined by the Graph API. Note that this interface is intended
 * to be used with GraphObject.Factory and not implemented directly.
 */
public interface GraphUser extends GraphObject {

    /**
     * Returns the birthday of the user.
     * 
     * @return the birthday of the user
     */
    String getBirthday();

    /**
     * Returns the first name of the user.
     * 
     * @return the first name of the user
     */
    String getFirstName();

    /**
     * Returns the ID of the user.
     * 
     * @return the ID of the user
     */
    String getId();

    /**
     * Returns the last name of the user.
     * 
     * @return the last name of the user
     */
    String getLastName();

    /**
     * Returns the Facebook URL of the user.
     * 
     * @return the Facebook URL of the user
     */
    String getLink();

    /**
     * Returns the current place of the user.
     * 
     * @return the current place of the user
     */
    GraphPlace getLocation();

    /**
     * Returns the middle name of the user.
     * 
     * @return the middle name of the user
     */
    String getMiddleName();

    /**
     * Returns the name of the user.
     * 
     * @return the name of the user
     */
    String getName();

    /**
     * Returns the Facebook username of the user.
     * 
     * @return the Facebook username of the user
     */
    String getUsername();

    /**
     * Sets the birthday of the user.
     * 
     * @param birthday the birthday of the user
     */
    void setBirthday(String birthday);

    /**
     * Sets the first name of the user.
     * 
     * @param firstName the first name of the user
     */
    void setFirstName(String firstName);

    /**
     * Sets the ID of the user.
     * 
     * @param id the ID of the user
     */
    void setId(String id);

    /**
     * Sets the last name of the user.
     * 
     * @param lastName the last name of the user
     */
    void setLastName(String lastName);

    /**
     * Sets the Facebook URL of the user.
     * 
     * @param link the Facebook URL of the user
     */
    void setLink(String link);

    /**
     * Sets the current place of the user.
     * 
     * @param location the current place of the user
     */
    void setLocation(GraphPlace location);

    /**
     * Sets the middle name of the user.
     * 
     * @param middleName the middle name of the user
     */
    void setMiddleName(String middleName);

    /**
     * Sets the name of the user.
     * 
     * @param name the name of the user
     */
    void setName(String name);

    /**
     * Sets the Facebook username of the user.
     * 
     * @param username the Facebook username of the user
     */
    void setUsername(String username);
}
