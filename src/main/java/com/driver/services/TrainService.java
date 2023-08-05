package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        List<Station> stations = trainEntryDto.getStationRoute();

        String route= "";
        int count=0;
        int size = stations.size();
        for(Station station: stations) {
            route += station;
            count++;
            if(count!=size) route+=",";
        }

        train.setRoute(route);

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        Train savedTrain = trainRepository.save(train);

        return Integer.valueOf(savedTrain.getTrainId());
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> ticketList = train.getBookedTickets();
        String[] arr = train.getRoute().split(",");
        HashMap<String, Integer> map = new HashMap<>();

        for(int i=0; i<arr.length; i++) {
            map.put(arr[i], i);
        }

        if(!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString()) || !map.containsKey(seatAvailabilityEntryDto.getToStation().toString())) {
            return 0;
        }

        int book = 0;
        for(Ticket ticket: ticketList) {
            book += ticket.getPassengersList().size();
        }

        Integer count = train.getNoOfSeats() - book;

        for(Ticket tkt: ticketList) {
            String fromStation = tkt.getFromStation().toString();
            String toStation = tkt.getToStation().toString();

            if(map.get(seatAvailabilityEntryDto.getToStation().toString()) <= map.get(toStation)) {
                count++;
            }
            else if(map.get(seatAvailabilityEntryDto.getFromStation().toString())>=map.get(toStation)) {
                count++;
            }
        }


       return count+2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        // split and match any route if matches then calculate person else throw exception

        String arr[] = train.getRoute().split(",");
        boolean flag = false;

        for(String stations: arr) {
             if(stations.equals(stations.toString())) {
                 flag = true;
                 break;
             }
        }

        if(flag==false) {
            throw new Exception("Train is not passing from this station");
        }

        List<Ticket> ticketList = train.getBookedTickets();
        Integer count = 0;
        for(Ticket ticket: ticketList) {
            if (ticket.getFromStation().toString().equals(station.toString())) {
                List<Passenger> passengerList = ticket.getPassengersList();

                count += passengerList.size();
            }
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        int maxAge = 0;
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> ticketList = train.getBookedTickets();

        for(Ticket ticket: ticketList) {
            for(Passenger passenger: ticket.getPassengersList()) {
                if(passenger.getAge()>maxAge) {
                    maxAge= passenger.getAge();
                }
            }
        }

        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainIds = new ArrayList<>();
        List<Train> trainList = trainRepository.findAll();

        for(Train train: trainList) {
            String arr[] = train.getRoute().split(",");

            int idx = -1;
            for(int i=0; i<arr.length; i++) {
                if(arr[i].equals(station.toString())) {
                    idx = i;
                    break;
                }
            }

            LocalTime time = train.getDepartureTime().plusHours(idx);
            if(time.equals(startTime) || time.equals(endTime) || (time.isAfter(startTime) && time.isBefore(endTime))) {
                trainIds.add(train.getTrainId());
            }
        }

        return trainIds;
    }

}
