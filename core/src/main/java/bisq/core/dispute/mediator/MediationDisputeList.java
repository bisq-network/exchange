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

package bisq.core.dispute.mediator;

import bisq.core.dispute.Dispute;
import bisq.core.dispute.DisputeList;
import bisq.core.proto.CoreProtoResolver;

import bisq.common.storage.Storage;

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
public final class MediationDisputeList extends DisputeList<MediationDisputeList> {

    MediationDisputeList(Storage<MediationDisputeList> storage) {
        super(storage);
    }

    @Override
    public void readPersisted() {
        MediationDisputeList persisted = storage.initAndGetPersisted(this, "MediationDisputeList", 0);
        if (persisted != null) {
            list.addAll(persisted.getList());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private MediationDisputeList(Storage<MediationDisputeList> storage, List<Dispute> list) {
        super(storage, list);
    }

    public static MediationDisputeList fromProto(protobuf.MediationDisputeList proto,
                                                 CoreProtoResolver coreProtoResolver,
                                                 Storage<MediationDisputeList> storage) {
        List<Dispute> list = proto.getDisputeList().stream()
                .map(disputeProto -> Dispute.fromProto(disputeProto, coreProtoResolver))
                .collect(Collectors.toList());
        list.forEach(e -> e.setStorage(storage));
        return new MediationDisputeList(storage, list);
    }
}
