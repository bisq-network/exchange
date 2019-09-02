/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dispute.arbitration;

import bisq.core.dispute.Dispute;
import bisq.core.dispute.DisputeList;
import bisq.core.proto.CoreProtoResolver;

import bisq.common.proto.ProtoUtil;
import bisq.common.storage.Storage;

import com.google.protobuf.Message;

import java.util.List;
import java.util.stream.Collectors;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
/*
 * Holds a List of Dispute objects.
 *
 * Calls to the List are delegated because this class intercepts the add/remove calls so changes
 * can be saved to disc.
 */
public final class ArbitrationDisputeList extends DisputeList<ArbitrationDisputeList> {

    ArbitrationDisputeList(Storage<ArbitrationDisputeList> storage) {
        super(storage);
    }

    @Override
    public void readPersisted() {
        // We need to use DisputeList as file name to not lose existing disputes which are stored in the DisputeList file
        ArbitrationDisputeList persisted = storage.initAndGetPersisted(this, "DisputeList", 50);
        if (persisted != null) {
            list.addAll(persisted.getList());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private ArbitrationDisputeList(Storage<ArbitrationDisputeList> storage, List<Dispute> list) {
        super(storage, list);
    }

    @Override
    public Message toProtoMessage() {
        return protobuf.PersistableEnvelope.newBuilder().setDisputeList(protobuf.DisputeList.newBuilder()
                .addAllDispute(ProtoUtil.collectionToProto(list))).build();
    }

    public static ArbitrationDisputeList fromProto(protobuf.DisputeList proto,
                                                   CoreProtoResolver coreProtoResolver,
                                                   Storage<ArbitrationDisputeList> storage) {
        List<Dispute> list = proto.getDisputeList().stream()
                .map(disputeProto -> Dispute.fromProto(disputeProto, coreProtoResolver))
                .collect(Collectors.toList());
        list.forEach(e -> e.setStorage(storage));
        return new ArbitrationDisputeList(storage, list);
    }
}
