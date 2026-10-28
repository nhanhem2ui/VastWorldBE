package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.sect.GetAllSectResponse;
import com.vastworld.vwbe.entities.Sect;
import com.vastworld.vwbe.repositories.MapRepository;
import com.vastworld.vwbe.repositories.PlayerLocationRepository;
import com.vastworld.vwbe.repositories.SectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SectService {
    private final SectRepository sectRepository;
    private final PlayerLocationRepository playerLocationRepository;

    public SectService(SectRepository sectRepository, PlayerLocationRepository playerLocationRepository) {
        this.sectRepository = sectRepository;
        this.playerLocationRepository =  playerLocationRepository;
    }

    public ServiceResult<List<GetAllSectResponse>> getAllCultivationRealms(UUID playerId, Integer region, boolean allRegions) {
        try {
            List<Sect> sects;

            if (allRegions) {
                sects = sectRepository.findAll();
            } else {
                if (region == null) {
                    var playerLocationOptional = playerLocationRepository.findByPlayer_Id(playerId);
                    if(playerLocationOptional.isEmpty()) {
                        return ServiceResult.failure("Something went wrong", HttpStatus.BAD_REQUEST);
                    }
                    var playerRegion = playerLocationOptional.get().getCurrentMap().getRegion();
                    if (playerRegion == null) {
                        return ServiceResult.failure("Require specify region", HttpStatus.BAD_REQUEST);
                    }
                    region = playerRegion.getRegionId();
                }
                sects = sectRepository.findByRegion_RegionId(region);
            }

            List<GetAllSectResponse> response = sects.stream()
                    .map(sect -> new GetAllSectResponse(
                            sect.getId(),
                            sect.getName(),
                            sect.getDescription(),
                            sect.getRegion() != null
                                    ? sect.getRegion().getRegionName()
                                    : null,
                            sect.getMap().getMapName(),
                            sect.getX(),
                            sect.getY(),
                            sect.getImageUrl()
                    ))
                    .toList();

            // Replace this with your project's actual success factory.
            return ServiceResult.success("Get sect success",response, HttpStatus.OK);

        } catch (Exception e) {
            return ServiceResult.failure("Failed to retrieve sects", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
