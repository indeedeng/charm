/*
 * Copyright (C) 2010 Indeed Inc.
 *
 * This file is part of CHARM.
 *
 * CHARM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CHARM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CHARM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.indeed.charm.model;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jackh
 * Date: Jul 5, 2010
 * Time: 6:22:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CommitInfo {
    long getNewRevision();

    String getAuthor();

    Date getDate();

    String getErrorMessage();
}
