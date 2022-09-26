/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.slack.install.enumerate;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class AmazonS3ObjectListingIterator implements Iterator<ObjectListing> {

    private final AmazonS3 s3;
    private ObjectListing listing;

    public AmazonS3ObjectListingIterator(AmazonS3 s3, String bucket, String prefix) {
        this.s3 = s3;

        ObjectListing seed = new ObjectListing();
        seed.setTruncated(true);
        seed.setBucketName(bucket);
        seed.setPrefix(prefix);

        this.listing = seed;
    }

    @Override
    public boolean hasNext() {
        return listing.isTruncated();
    }

    @Override
    public ObjectListing next() {
        return listing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(listing.getBucketName())
                .withPrefix(listing.getPrefix())
                .withMarker(listing.getNextMarker())
        );
    }

    public Stream<ObjectListing> toStream() {
        Spliterator<ObjectListing> spliterator = Spliterators.spliteratorUnknownSize(this, 0);
        return StreamSupport.stream(spliterator, false);
    }

}
