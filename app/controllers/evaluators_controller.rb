class EvaluatorsController < ApplicationController
  def show
    data = {:evaluators => [{:id => "ev_0"}, {:id => "ev_1"}, {:id => "ev_2"}, {:id => "ev_3"}, {:id => "ev_4"}, ]}
    render :json => data
  end

  def status
    data = {:evaluator_id => params[:id], :status => "AVAILABLE", :assigned_task_id => "task_"+params[:id]}
    render :json => data
  end

  def resource
    data = {:evaluator_id => params[:id], :resource => params[:name], :point => get_random_point}
    render :json => data
  end

  def timeseries
    data = {:evaluator_id => params[:id], :resource => params[:name], :points => get_random_points}
    render :json => data
  end

  private
  def get_random_point(offset=0)
    time = js_current_time + offset*1000
    rand = Random.new(Random.new_seed)
    return {:time => time, :value => rand.rand}
  end

  def get_random_points
    data = Array.new

    i = -19

    while i <= 0 do
      data.push(get_random_point i)
      i = i+1
    end
    return data
  end

  def js_current_time
    time = Time.new.getlocal
    time = time.to_i * 1000
    return time
  end
end
